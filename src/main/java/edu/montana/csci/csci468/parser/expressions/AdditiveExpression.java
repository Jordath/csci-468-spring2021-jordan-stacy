package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Opcodes;

public class AdditiveExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public AdditiveExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }
    public Expression getRightHandSide() {
        return rightHandSide;
    }
    public boolean isAdd() {
        return operator.getType() == TokenType.PLUS;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
        if (getType().equals(CatscriptType.INT)) {
            if (!leftHandSide.getType().equals(CatscriptType.INT)) {
                leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            if (!rightHandSide.getType().equals(CatscriptType.INT)) {
                rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
        // TODO handle strings
    }

    @Override
    public CatscriptType getType() {
        if (leftHandSide.getType().equals(CatscriptType.STRING) || rightHandSide.getType().equals(CatscriptType.STRING)) {
            return CatscriptType.STRING;
        } else {
            return CatscriptType.INT;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        if (getType() == CatscriptType.STRING){
            Object lhsValue = leftHandSide.evaluate(runtime);
            Object rhsValue = rightHandSide.evaluate(runtime);
            return String.valueOf(lhsValue) + String.valueOf(rhsValue);
            /*
            if(leftHandSide.getType() == CatscriptType.NULL){
                return "null" + rightHandSide.evaluate(runtime).toString();
            }
            else if(rightHandSide.getType() == CatscriptType.NULL){
                return leftHandSide.evaluate(runtime).toString() + "null";
            }
            else {
                return leftHandSide.evaluate(runtime).toString() + rightHandSide.evaluate(runtime).toString();
            }
            */
        }
        else {
            Integer lhsValue = (Integer) leftHandSide.evaluate(runtime);
            Integer rhsValue = (Integer) rightHandSide.evaluate(runtime);
            //TODO handle string case
            if (isAdd()) {
                return lhsValue + rhsValue;
            } else {
                return lhsValue - rhsValue;
            }
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        getLeftHandSide().transpile(javascript);
        javascript.append(operator.getStringValue());
        getRightHandSide().transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {

        if (isAdd()) {
            if((leftHandSide.getType() == CatscriptType.INT) && (rightHandSide.getType() == CatscriptType.INT)) {
                getLeftHandSide().compile(code);
                getRightHandSide().compile(code);
                code.addInstruction(Opcodes.IADD);
            }
            else {
                    getLeftHandSide().compile(code);
                    box(code, getLeftHandSide().getType());
                    code.addMethodInstruction(Opcodes.INVOKESTATIC, ByteCodeGenerator.internalNameFor(String.class), "valueOf",
                            "(Ljava/lang/Object;)Ljava/lang/String;");

                    getRightHandSide().compile(code);
                    box(code, getRightHandSide().getType());
                    code.addMethodInstruction(Opcodes.INVOKESTATIC, ByteCodeGenerator.internalNameFor(String.class), "valueOf",
                            "(Ljava/lang/Object;)Ljava/lang/String;");

                code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, ByteCodeGenerator.internalNameFor(String.class), "concat",
                        "(Ljava/lang/String;)Ljava/lang/String;");
            }
        } else{
            getLeftHandSide().compile(code);
            getRightHandSide().compile(code);
            code.addInstruction(Opcodes.ISUB);
        }
    }

}
