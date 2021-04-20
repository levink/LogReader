package com.logger;

import android.app.Application;

import com.logger.classes.LogWriter;
import com.logger.db.DBHelper;

public class App extends Application {
    private DBHelper db;
    private LogWriter writer;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = new DBHelper(instance);
        writer = new LogWriter(this, "results");
    }

    private static App instance;
    public static DBHelper getDB() {
        return instance.db;
    }
    public static LogWriter getLogWriter() {
        return instance.writer;
    }
}
