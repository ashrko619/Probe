package com.probe.builtin;

import java.util.List;

import com.probe.Callable;
import com.probe.Interpreter;
import com.probe.ProbeArray;

@Expose("len")
public class Len implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object obj = arguments.get(0);
        if (obj instanceof String) {
            return (double) ((String) obj).length();
        } else if (obj instanceof ProbeArray) {
            return (double) ((ProbeArray) obj).size();
        }
        throw new ExecutionException("Expected string or array");
    }

    @Override
    public int parameters() {
        return 1;
    }
}
