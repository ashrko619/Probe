package com.probe;


public class Return extends RuntimeException {
    final Object result;

    public Return(Object result) {
        this.result = result;
    }

    public Return() {
        this(null);
    }
}
