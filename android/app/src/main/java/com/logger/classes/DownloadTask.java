
package com.logger.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Runnable {

    public interface Callback {
        void onStart(long contentLength) throws IllegalArgumentException;
        void onBlockDownload(byte[] block, int size) throws IOException;
        void onComplete();
        void onCancel();
        void onFail(String errorMessage);
        void link(DownloadTask task);
    }

    private final String url;
    private final Callback callback;
    private volatile boolean canceled;

    public DownloadTask(String url, Callback callback) {
        this.url = url;
        this.canceled = false;
        this.callback = callback;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String message = "Bad response. " +
                        "Code=" + code + ". " +
                        "Message=" + connection.getResponseMessage();
                callback.onFail(message);
                return;
            }

            int length = connection.getContentLength();
            try (InputStream stream = connection.getInputStream()) {
                parse(stream, length);
            }
        } catch (IOException | IllegalArgumentException ex) {
            callback.onFail(ex.getMessage());
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
    }
    private void parse(InputStream stream, int length) throws IOException, IllegalArgumentException {
        final int BLOCK_SIZE = 8192;
        byte[] data = new byte[BLOCK_SIZE];

        callback.onStart(length);
        int readCount = stream.read(data);
        while(readCount > -1) {
            callback.onBlockDownload(data, readCount);
            if (canceled) {
                break;
            }
            readCount = stream.read(data);
        }
        if (canceled) {
            callback.onCancel();
        } else {
            callback.onComplete();
        }
    }
    public void cancel() {
        canceled = true;
    }
}