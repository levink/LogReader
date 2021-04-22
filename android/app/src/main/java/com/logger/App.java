package com.logger;

import android.app.Application;

import com.logger.classes.DownloadTask;
import com.logger.db.DBHelper;
import com.logger.db.DBRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private DBHelper db;
    private ExecutorService dbPool;
    private ExecutorService networkPool;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dbPool = Executors.newSingleThreadExecutor();
        networkPool = Executors.newSingleThreadExecutor();
        db = new DBHelper(instance);
    }


    private static App instance;
    public static App getInstance() {
        return instance;
    }
    public static void dbWork(DBRunnable runnable) {
        instance.dbPool.execute(() ->
            runnable.run(instance.db)
        );
    }
    public static void download(String url, DownloadTask.Callback callback) {
        DownloadTask task = new DownloadTask(url, callback);
        callback.link(task);
        instance.networkPool.execute(task);
    }


}
