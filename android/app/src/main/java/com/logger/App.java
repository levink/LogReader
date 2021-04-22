package com.logger;

import android.app.Application;

import com.logger.classes.Repo;
import com.logger.classes.logs.LogWriter;
import com.logger.db.DBHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    @FunctionalInterface
    public interface DBRunnable {
        void run(DBHelper db);
    }

    private DBHelper db;
    private Repo repo;
    private ExecutorService dbPool;
    private ExecutorService repoPool;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dbPool = Executors.newSingleThreadExecutor();
        repoPool = Executors.newSingleThreadExecutor();
        db = new DBHelper(instance);
        repo = new Repo(repoPool);
    }

    private static App instance;
    public static App getInstance() {
        return instance;
    }
    public static void dbWork(DBRunnable runnable) {
        instance.dbPool.execute(() -> {
            runnable.run(instance.db);
        });
    }
    public static Repo getRepo() {
        return instance.repo;
    }
}
