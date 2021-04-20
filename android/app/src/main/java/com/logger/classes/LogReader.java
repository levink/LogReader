package com.logger.classes;

public class LogReader {
    static {
        System.loadLibrary("native-lib");
    }

    private native boolean setFilter(String filter);
    public native boolean addBlock(byte[] block, int count);
    public native void parseLast();

    @FunctionalInterface
    public interface OnMatchCallback {
        void onMatch(String item);
    }

    private final OnMatchCallback callback;
    public LogReader(String filter, OnMatchCallback callback) {
        setFilter(filter);
        this.callback = callback;
    }

    /* This method called from C++ */
    @SuppressWarnings("unused")
    public void saveItem(String item) {
        if (callback != null)
            callback.onMatch(item);
    }
}
