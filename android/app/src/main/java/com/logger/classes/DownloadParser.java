package com.logger.classes;

import com.logger.classes.logs.LogReader;
import com.logger.classes.logs.LogWriter;
import com.logger.model.Match;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class DownloadParser implements DownloadTask.Callback {

    private final String filter;
    private final LogWriter writer;
    private final LogReader reader;
    private final LinkedBlockingQueue<Match> queue;
    private long length;
    private long total;
    private boolean cancelled;
    private volatile boolean paused;
    private volatile long pauseTime = 0;
    private DownloadTask task;

    public DownloadParser(String filter, LogReader reader, LogWriter writer) {
        this.filter = filter;
        this.writer = writer;
        this.reader = reader;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onStart(long contentLength) throws IllegalArgumentException{
        writer.write("Parsing start");
        boolean ok = reader.setFilter(filter);
        if (!ok) {
            throw new IllegalArgumentException("Bad filter: " + filter);
        }
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
    public void onBlockDownload(byte[] block, int size) throws IOException {
        total += size;

        boolean added = reader.addBlock(block, size);
        if (!added) {
            throw new IOException("Can not add new data block");
        }
        checkMatches();
        checkProgress();

        if (paused && !cancelled) {
            long now = System.currentTimeMillis();
            long delta = now - pauseTime;
            if (delta > 1000) {
                cancel();
            }
        }
    }
    private void checkMatches() {
        if (queue.isEmpty() || cancelled) {
            return;
        }
        onMatch(queue);
    }
    private void checkProgress() {
        if (cancelled) {
            return;
        }
        int progress = calcProgress(total, length);
        onProgress(progress);
    }
    private int calcProgress(long downloadedSize, long fullSize) {
        if (fullSize <= 0) return -1;
        if (downloadedSize >= fullSize) return 100;
        return (int)((100.f * downloadedSize) / fullSize);
    }
    protected abstract void onProgress(int progressValue);
    protected abstract void onMatch(LinkedBlockingQueue<Match> queue);

    @Override
    public void onComplete() {
        reader.parseLast();
        checkProgress();
        checkMatches();
        writer.write("Parsing complete");
        writer.close();
    }

    @Override
    public void onCancel() {
        writer.write("Parsing canceled");
        writer.close();
    }

    @Override
    public void onFail(String errorMessage) {
        writer.write("Parsing failed. " + errorMessage);
        writer.close();
    }

    @Override
    public void link(DownloadTask task) {
        this.task = task;
    }

    public void cancel() {
        cancelled = true;
        if (task != null) {
            task.cancel();
        }
        task = null;
        queue.clear();
    }

    public void pause() {
        pauseTime = System.currentTimeMillis();
        paused = true;
    }

    public void resume() {
        paused = false;
    }
}
