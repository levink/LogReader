
package com.logger.task;

import com.logger.App;
import com.logger.classes.logs.LogReader;
import com.logger.classes.logs.LogWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadTask implements Runnable {

    public interface Listener {
        void onMatch(List<String> items);
        void onProgress(int progress);
        void onComplete();
        void onCancel();
        void onFail();
    }

    private enum Status {
        READY,
        RUNNING,
        CANCELLED,
        FAILED,
        FINISHED
    }

    private final String url;
    private final String mask;
    private final LogWriter writer;
    private volatile boolean paused;
    private volatile long stopTime;
    private volatile Status status;
    private volatile int progress;
    private Listener listener;

    private DownloadTask(String url, String mask) {
        this.url = url;
        this.mask = mask;
        this.writer = App.getLogWriter();
        this.paused = false;
        this.stopTime = 0;
        this.status = Status.READY;
        this.progress = 0;
    }

    @Override
    public void run() {

        HttpURLConnection connection = null;
        try {
            status = Status.RUNNING;
            connection = openGET(url);

            boolean badConnection = bad(connection);
            if (badConnection)
                return;

            int length = connection.getContentLength();
            if (length <= 0)
                return;

            InputStream stream = connection.getInputStream();
            parse(stream, length);
            stream.close();
            finish();
        }
        catch (IOException ignored) {
            status = Status.FAILED;
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
    }
    private HttpURLConnection openGET(String url) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }
    private boolean bad(HttpURLConnection connection) throws IOException {
        if (connection == null) {
            return true;
        }

        int code = connection.getResponseCode();
        return code != HttpURLConnection.HTTP_OK;
    }
    private void parse(InputStream stream, int length) throws IOException {

        final LinkedBlockingQueue<String> results = new LinkedBlockingQueue<>();
        LogReader reader = new LogReader(mask, item-> {
            writer.write(item);
            results.offer(item);
        });

        final int BLOCK_SIZE = 8192;
        byte[] data = new byte[BLOCK_SIZE];

        long total = 0;
        int readCount = stream.read(data);
        while(readCount > -1 && status == Status.RUNNING) {
            total += readCount;
            boolean isOk = reader.addBlock(data, readCount);
            if (isOk) {
                progress = (int)(100 * total / length);
                onProgress(progress);
                onMatch(results);
            }
            else {
                status = Status.FAILED;
                break;
            }

            if (paused) {
                long delta = System.currentTimeMillis() - stopTime;
                if (delta > 1000) {
                    status = Status.CANCELLED;
                    break;
                }
            }

            readCount = stream.read(data);
        }
        if (status == Status.RUNNING) {
            reader.parseLast();
        }

        onMatch(results);
    }

    public boolean isComplete() {
        switch (status) {
            case FINISHED:
            case FAILED:
            case CANCELLED:
                return true;
            default:
                return false;
        }
    }
    public int getProgress() {
        return progress;
    }

    public synchronized void addListener(Listener listener) {
        this.listener = listener;
        paused = false;
    }
    public synchronized void removeListener() {
        this.listener = null;
        paused = true;
        stopTime = System.currentTimeMillis();
    }
    private synchronized void onMatch(LinkedBlockingQueue<String> queue) {
        if (listener != null && !queue.isEmpty()) {
            ArrayList<String> items = new ArrayList<>();
            queue.drainTo(items);
            listener.onMatch(items);
        }
    }
    private synchronized void onProgress(int progress) {
        if (listener != null)
            listener.onProgress(progress);
    }
    private synchronized void finish() {
        switch (status) {
            case RUNNING: onComplete(); break;
            case FAILED: onFail(); break;
            case CANCELLED: onCancel(); break;
        }
        status = Status.FINISHED;
    }
    private void onComplete() {
        writer.write("Task completed");
        writer.close();

        if (listener != null)
            listener.onComplete();
    }
    private void onCancel() {
        writer.write("Task cancelled");
        writer.close();

        if (listener != null)
            listener.onCancel();
    }
    private void onFail() {
        writer.write("Task failed");
        writer.close();

        if (listener != null)
            listener.onFail();
    }

    private static final ExecutorService staticPool = Executors.newSingleThreadExecutor();
    public void execute() {
        staticPool.execute(this);
    }
    public synchronized void cancel() {
        if (status != Status.FINISHED) {
            status = Status.CANCELLED;
        }
    }


    private static DownloadTask task = null;
    public static DownloadTask getOrCreate(String url, String mask) {
        if (task == null) {
            task = new DownloadTask(url, mask);
            return task;
        }

        if (task.isComplete()) {

        }

        return task;
    }
}