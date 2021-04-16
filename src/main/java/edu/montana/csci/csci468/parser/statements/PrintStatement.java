package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

import java.io.PrintStream;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class PrintStatement extends Statement {
    private Expression expression;

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }


    public Expression getExpression() {
        return expression;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        getProgram().print(expression.evaluate(runtime));
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addVarInstruction(Opcodes.ALOAD, 0);
        getExpression().compile(code);
        box(code, getExpression().getType());
        code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(CatScriptProgram.class),
                "print", "(Ljava/lang/Object;)V");

//        Integer localStorageSlotFor = code.createLocalStorageSlotFor(expression.toString());
//        code.addMethodInstruction(Opcodes.GETSTATIC, ByteCodeGenerator.internalNameFor(System.class),
//                "out", "L"+ ByteCodeGenerator.internalNameFor(PrintStream.class) + ";");
//        code.addVarInstruction(Opcodes.ALOAD, localStorageSlotFor);
//        code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, ByteCodeGenerator.internalNameFor(PrintStream.class), "println",
//                "(L" + ByteCodeGenerator.internalNameFor(expression.getType().getJavaType()) +";)V");
    }

}
