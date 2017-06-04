package com.probe.builtin;


import java.util.HashMap;
import java.util.Map;

import com.probe.Callable;

public class BuiltIns {
    public static Map<String, Callable> callables = new HashMap<>();

    static {
        register(Len.class, new Len());
        register(Set.class, new Set());
    }

    static <T extends Callable> void register(Class<T> aClass, T instance) {

        if (!aClass.isAnnotationPresent(Expose.class)) {
            throw new RuntimeException(aClass.getSimpleName() + " does not have Expose annotation");
        }

        Expose expose = aClass.getAnnotation(Expose.class);
        callables.put(expose.value(), instance);

    }
}
