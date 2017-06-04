package com.probe;


public class ProbeArray {
    private final Object[] arr;

    public ProbeArray(Object[] arr) {
        this.arr = arr;
    }

    public int size() {
        return arr.length;
    }

    public Object get(int index) {
        return arr[index];
    }

}
