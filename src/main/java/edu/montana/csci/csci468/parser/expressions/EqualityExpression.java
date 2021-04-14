package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        if(operator.equals("==")){
            return getLeftHandSide() == getRightHandSide();
        }
        else{
            return getLeftHandSide() != getRightHandSide();
        }

        //return super.evaluate(runtime);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Label L1 = new Label();
        Label L2 = new Label();
        getLeftHandSide().compile(code);
        getRightHandSide().compile(code);
        if (isEqual()) {
            //if(leftHandSide.getType() == CatscriptType.INT && rightHandSide.getType() == CatscriptType.INT) {
                code.addJumpInstruction(Opcodes.IF_ICMPNE, L1);
                code.addInstruction(Opcodes.ICONST_1);
                code.addJumpInstruction(Opcodes.GOTO, L2);
                code.addLabel(L1);
                code.addInstruction(Opcodes.ICONST_0);
                code.addLabel(L2);
            //}
//            else if (leftHandSide.getType() == CatscriptType.NULL || rightHandSide.getType() == CatscriptType.NULL){
//                code.addJumpInstruction(Opcodes.IFNONNULL, L1);
//                code.addInstruction(Opcodes.ICONST_1);
//                code.addJumpInstruction(Opcodes.GOTO, L2);
//                code.addLabel(L1);
//                code.addInstruction(Opcodes.ICONST_0);
//                code.addLabel(L2);
//
//            }
        }
        if(!isEqual()){
            code.addJumpInstruction(Opcodes.IF_ICMPEQ, L1);
            code.addInstruction(Opcodes.ICONST_1);
            code.addJumpInstruction(Opcodes.GOTO, L2);
            code.addLabel(L1);
            code.addInstruction(Opcodes.ICONST_0);
            code.addLabel(L2);

        }

        //super.compile(code);
    }


}
