package com.logger;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.logger.classes.DownloadTask;
import com.logger.classes.Repo;
import com.logger.classes.logs.LogReader;
import com.logger.classes.logs.LogWriter;
import com.logger.model.Match;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class AppViewModel extends ViewModel {

    public static AppViewModel getInstance(@NonNull ViewModelStoreOwner owner) {
        ViewModelProvider provider = new ViewModelProvider(owner);
        return provider.get(AppViewModel.class);
    }

    private MutableLiveData<String> url;
    private MutableLiveData<String> mask;
    private MutableLiveData<List<String>> urlHistory;
    private MutableLiveData<List<String>> maskHistory;
    private MutableLiveData<LinkedList<String>> matches;
    private MutableLiveData<Integer> progress;
    private MutableLiveData<Boolean> selectAll;
    private ParseListener downloadListener;

    public LiveData<String> getUrl() {
        if (url == null){
            url = new MutableLiveData<>();
        }
        return url;
    }
    public LiveData<String> getMask() {
        if (mask == null){
            mask = new MutableLiveData<>();
        }
        return mask;
    }
    public LiveData<List<String>> getUrlHistory() {
        if (urlHistory == null) {
            urlHistory = new MutableLiveData<>();
            App.dbWork(db -> {
                List<String> items = db.getUrlHistory();
                urlHistory.postValue(items);
            });
        }
        return urlHistory;
    }
    public LiveData<List<String>> getMaskHistory() {
        if (maskHistory == null) {
            maskHistory = new MutableLiveData<>();
            App.dbWork(db -> {
                List<String> items = db.getMaskHistory();
                maskHistory.postValue(items);
            });
        }
        return maskHistory;
    }
    public LiveData<LinkedList<String>> getMatchesQueue() {
        if (matches == null) {
            matches = new MutableLiveData<>(new LinkedList<>());
        }
        return matches;
    }
    public LiveData<Integer> getProgress() {
        if (progress == null) {
            progress = new MutableLiveData<>(0);
        }
        return progress;
    }
    public LiveData<Boolean> getSelectAll() {
        if (selectAll == null) {
            selectAll = new MutableLiveData<>(true);
        }
        return selectAll;
    }

    public void setUrl(String value) {
         url.setValue(value);
    }
    public void setMask(String value) {
        mask.setValue(value);
    }
    public void clearMaskHistory() {
        App.dbWork(db -> {
            db.clearMaskHistory();
            List<String> items = db.getMaskHistory();
            maskHistory.postValue(items);
        });
    }
    public void switchSelectAll() {
        Boolean checked = selectAll.getValue();
        if (checked != null) {
            selectAll.setValue(!checked);
        }
    }
    public void startSearch(String url, String mask) {

        this.url.setValue(url);
        this.mask.setValue(mask);

        App.dbWork(db -> {
            db.saveUrl(url);
            db.saveMask(mask);
            urlHistory.postValue(db.getUrlHistory());
            maskHistory.postValue(db.getMaskHistory());
        });

        if (downloadListener != null) {
            downloadListener.cancel();
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        LogWriter writer = new LogWriter(App.getInstance());
        LogReader reader = new LogReader();
        downloadListener = new ParseListener(mask, writer, reader, new ParseListener.Callback() {
            @Override
            public void onProgress(int progressValue) {
                progress.postValue(progressValue);
            }

            @Override
            public void onMatch(LinkedBlockingQueue<String> queue) {
                mainHandler.post(() -> {
                    LinkedList<String> list = matches.getValue();
                    queue.drainTo(list);
                    matches.setValue(list);
                });
            }

//            @Override
//            public void onMatch(String item) {
//                mainHandler.post(() -> {
//                    LinkedList<String> value = matches.getValue();
//                    value.add(item);
//                    matches.setValue(value);
//                });
//            }
        });

        Repo repo = App.getRepo();
        repo.startDownload(url, downloadListener);
    }

    public void pauseSearch() {
        if (downloadListener != null)
            downloadListener.pause();
    }
    public void resumeSearch() {
        if (downloadListener != null)
            downloadListener.resume();
    }
    public void cancelSearch() {
        if (downloadListener != null)
            downloadListener.cancel();
    }

    public static class ParseListener implements DownloadTask.Callback {

        public interface Callback {
            void onProgress(int progress);
            void onMatch(LinkedBlockingQueue<String> queue);
            //void onMatch(String item);
        }

        private final String filter;
        private final LogWriter writer;
        private final LogReader reader;
        private final Callback callback;
        private final LinkedBlockingQueue<String> queue;
        private long length;
        private long total;
        private boolean cancelled;
        private volatile boolean paused;
        private volatile long pauseTime = 0;
        private DownloadTask task;

        public ParseListener(String filter, LogWriter writer, LogReader reader, Callback callback) {
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
                queue.offer(item);
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
                    cancelTask();
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

        public void link(DownloadTask task) {
            this.task = task;
        }

        public void pause() {
            pauseTime = System.currentTimeMillis();
            paused = true;
        }

        public void resume() {
            paused = false;
        }

        public void cancel() {
            cancelTask();
        }

        private void cancelTask() {
            cancelled = true;
            if (task != null) {
                task.cancel();
            }
            task = null;
        }
    }
}
