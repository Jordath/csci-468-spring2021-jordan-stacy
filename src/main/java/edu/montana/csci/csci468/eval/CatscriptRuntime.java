package edu.montana.csci.csci468.eval;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// TODO - implement proper scoping
public class CatscriptRuntime {
    LinkedList<Map<String, Object>> scopes = new LinkedList<>();
    HashMap<String, Object> globalScope;

    public CatscriptRuntime(){

        globalScope = new HashMap<>();
        scopes.push(globalScope);
    }

    public Object getValue(String name) {
        Object localValue = scopes.peek().get(name);
        if (localValue != null){
            return localValue;
        }
        else {
            return globalScope.get(name);
        }
    }

    public void setValue(String variableName, Object val) {
        globalScope.put(variableName, val);
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

}
