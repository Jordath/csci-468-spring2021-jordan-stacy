package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.parser.ParseElement;

public abstract class Expression extends ParseElement {
    public Object evaluate() {
        throw new UnsupportedOperationException("evaluate needs to be implemented for " + this.getClass().getName());
    }

    @Override
    public void transpile(StringBuilder javascript) {
        throw new UnsupportedOperationException("transpile needs to be implemented for " + this.getClass().getName());
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        throw new UnsupportedOperationException("compile needs to be implemented for " + this.getClass().getName());
    }
}