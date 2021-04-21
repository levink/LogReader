package com.logger;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.logger.classes.LogWriter;
import com.logger.db.DBHelper;
import com.logger.db.DBRepo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    @FunctionalInterface
    public interface DBRunnable {
        void run(DBHelper db);
    }

    private DBHelper db;
    private ExecutorService dbPool;
    private LogWriter writer;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = new DBHelper(instance);
        dbPool = Executors.newSingleThreadExecutor();
        writer = new LogWriter(this, "results");
    }

    private static App instance;
    public static void dbWork(DBRunnable runnable) {
        instance.dbPool.execute(() -> {
            runnable.run(instance.db);
        });
    }
    public static LogWriter getLogWriter() {
        return instance.writer;
    }
}
