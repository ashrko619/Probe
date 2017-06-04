package com.probe;

import java.util.List;

class ProbeFunction implements Callable {
    private final FunctionDeclaration declaration;
    private final Environment closure;

    ProbeFunction(FunctionDeclaration declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object result = null;
        try {
            final Environment env = new Environment(closure);
            for (int i = 0; i < declaration.parameters.size(); ++i) {
                env.define(declaration.parameters.get(i).lexeme, arguments.get(i));
            }
            interpreter.executeBody(declaration.body, env);
        } catch (Return returnValue) {
            result = returnValue.result;
        }
        return result;
    }

    @Override
    public int parameters() {
        return declaration.parameters.size();
    }
}
