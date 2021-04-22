package com.logger.classes.logs;

public class LogReader {
    static {
        System.loadLibrary("native-lib");
    }

    public native boolean setFilter(String filter);
    public native boolean addBlock(byte[] block, int count);
    public native void parseLast();

    @FunctionalInterface
    public interface OnMatchCallback {
        void onMatch(String item);
    }

    private OnMatchCallback callback;
    public void onMatch(OnMatchCallback callback) {
        this.callback = callback;
    }

    /* This method called from C++ */
    @SuppressWarnings("unused")
    public void saveItem(String item) {
        if (callback != null)
            callback.onMatch(item);
    }
}
