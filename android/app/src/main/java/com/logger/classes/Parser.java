package com.logger.classes;

import android.util.Log;

import com.logger.classes.logs.LogReader;
import com.logger.classes.logs.LogWriter;
import com.logger.model.Match;

import java.util.concurrent.LinkedBlockingQueue;

public class Parser implements DownloadTask.Callback {

    public interface ParseCallback {
        void onProgress(int progress);
        void onMatch(LinkedBlockingQueue<Match> queue);
    }

    private final String filter;
    private final LogWriter writer;
    private final LogReader reader;
    private final ParseCallback callback;
    private final LinkedBlockingQueue<Match> queue;
    private long length;
    private long total;
    private boolean cancelled;
    private volatile boolean paused;
    private volatile long pauseTime = 0;
    private DownloadTask task;

    public Parser(String filter, LogReader reader, LogWriter writer, ParseCallback callback) {
        this.filter = filter;
        this.writer = writer;
        this.reader = reader;
        this.callback = callback;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onStart(long contentLength) {
        Log.d("test123", "Parsing start");
        writer.write("Parsing start");
        reader.setFilter(filter);
        reader.onMatch(item -> {
            writer.write(item);
            queue.offer(new Match(item, false));
        });
        length = contentLength;
        total = 0;
        paused = false;
        cancelled = false;
    }

    @Override
    public void onProgress(byte[] block, int size) {
        total += size;

        reader.addBlock(block, size);
        callback.onMatch(queue);

        int progress = (int)(length <= 0 ? -1 : (100 * total / length));
        callback.onProgress(progress);

        if (paused && !cancelled) {
            long now = System.currentTimeMillis();
            long delta = now - pauseTime;
            if (delta > 1000) {
                cancel();
            }
        }
    }

    @Override
    public void onComplete() {
        reader.parseLast();
        callback.onMatch(queue);
        Log.d("test123", "Parsing complete");
        writer.write("Parsing complete");
        writer.close();
    }

    @Override
    public void onCancel() {
        writer.write("Parsing canceled");
        writer.close();
    }

    @Override
    public void onFail() {
        writer.write("Parsing failed");
        writer.close();
    }

    @Override
    public void link(DownloadTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        cancelled = true;
        if (task != null) {
            task.cancel();
        }
        task = null;
    }

    public void pause() {
        pauseTime = System.currentTimeMillis();
        paused = true;
    }

    public void resume() {
        paused = false;
    }
}
