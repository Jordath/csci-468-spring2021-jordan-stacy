package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import edu.montana.csci.csci468.parser.expressions.FunctionCallExpression;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class FunctionCallStatement extends Statement {
    private FunctionCallExpression expression;
    public FunctionCallStatement(FunctionCallExpression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public List<Expression> getArguments() {
        return expression.getArguments();
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
    }

    public String getName() {
        return expression.getName();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        expression.evaluate(runtime);

        //super.execute(runtime);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addVarInstruction(Opcodes.ALOAD, 0);
        for (Expression argument : expression.getArguments()) {
            //code.pushConstantOntoStack(argument);
//            //argument.compile(code);
//            if(argument.getType() == CatscriptType.OBJECT){
//                box(code, argument.getType());
//            }

        }
        //expression.compile(code);

//        code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(CatScriptProgram.class), getName(),
//                "(Ljava/lang/Object;)V);");
        //expression.compile(code);
        //super.compile(code);
    }
}
