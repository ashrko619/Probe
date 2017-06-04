package com.probe.builtin;

import java.util.List;

import com.probe.Callable;
import com.probe.Interpreter;
import com.probe.ProbeArray;

@Expose("set")
public class Set implements Callable {
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object obj = arguments.get(0);
        if (obj instanceof String) {
            return ((String) obj);
        } else if (obj instanceof ProbeArray) {
            ProbeArray array = (ProbeArray) obj;
            //ProbeArray newArray = new ProbeArray(((ProbeArray) obj).size());
            Object objects[] = new Object[array.size()];
            for (int i = 0; i < objects.length; ++i) {
                objects[i] = array.get(i);
            }
            double pos = (double) arguments.get(1);
            int ipos = (int) pos;
            objects[ipos] = arguments.get(2);
            return new ProbeArray(objects);
        }
        throw new ExecutionException("Expected string or array");
    }

    @Override
    public int parameters() {
        return 3;
    }
}
