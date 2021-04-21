package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class VariableStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType explicitType;
    private CatscriptType type;

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setExplicitType(CatscriptType type) {
        this.explicitType = type;
    }

    public CatscriptType getExplicitType() {
        return explicitType;
    }

    public boolean isGlobal() {
        return getParent() instanceof CatScriptProgram;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            // TODO if there is an explicit type, ensure it is correct
            //      if not, infer the type from the right hand side expression
            if(explicitType != null){
                type = explicitType;
                // and verify compatibility with expression
                if(!type.isAssignableFrom(expression.getType())){
                    addError(ErrorType.INCOMPATIBLE_TYPES);
                }
            }
            else {
                type = expression.getType(); // type inference
            }
            symbolTable.registerSymbol(variableName, type);
        }
    }

    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        runtime.setValue(variableName,expression.evaluate(runtime));
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        if(isGlobal()){
            code.addVarInstruction(Opcodes.ALOAD, 0);
            if(getExpression().getType() == CatscriptType.INT || getExpression().getType() == CatscriptType.BOOLEAN) {
                expression.compile(code);
                code.addField(variableName, "I");
                //code.addFieldInstruction(Opcodes.ISTORE, variableName, "I", code.getProgramInternalName());
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "I", code.getProgramInternalName());
                //code.addInstruction(Opcodes.RETURN);
            }
            else {
                expression.compile(code);
                code.addField(variableName, "L" + ByteCodeGenerator.internalNameFor(getType().getJavaType()) + ";");
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "L" + ByteCodeGenerator.internalNameFor(getType().getJavaType()) + ";"
                        , code.getProgramInternalName());
                //code.addInstruction(Opcodes.RETURN);
            }

            //box(code, type);

//            expression.compile(code);

            //code.addFieldInstruction(Opcodes.ISTORE, variableName, );

        }
        if(!isGlobal()) {
            Integer localStorageSlotForVariable = code.createLocalStorageSlotFor(variableName);
            if(getExpression().getType() == CatscriptType.INT || getExpression().getType() == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ISTORE, localStorageSlotForVariable);
            }
            else{
                code.addVarInstruction(Opcodes.ASTORE, localStorageSlotForVariable);
            }
        }

        //super.compile(code);
    }
}
