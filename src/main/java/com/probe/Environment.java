package com.probe;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Environment parent;
    private final Map<String, Object> values;

    Environment() {
        this(null);
    }

    Environment(Environment parent) {
        this.parent = parent;
        values = new HashMap<>();
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (isDefined(name)) {
            return values.get(name.lexeme);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.", ErrorReporter.Mode.COVER);
    }


    void assign(Token name, Object value) {
        if (isDefined(name)) {
            values.put(name.lexeme, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.", ErrorReporter.Mode.COVER);
    }

    private boolean isDefined(Token name) {
        return values.containsKey(name.lexeme);
    }

}
