package com.probe;

import java.util.List;

public interface Callable {
    Object call(Interpreter interpreter, List<Object> arguments);

    int parameters();
}
