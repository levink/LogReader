package com.logger.classes;

import com.logger.AppViewModel;

import java.util.concurrent.Executor;

public class Repo {

    private final Executor pool;
    public Repo(Executor pool) {
        this.pool = pool;
    }

    public void startDownload(String url, AppViewModel.ParseListener callback) {
        DownloadTask task = new DownloadTask(url, callback);
        callback.link(task);
        pool.execute(task);
    }
}
