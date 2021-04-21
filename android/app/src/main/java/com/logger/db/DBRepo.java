package com.logger.db;

import java.util.concurrent.ExecutorService;

public class DBRepo {

    @FunctionalInterface
    public interface RepoRunnable {
        void run(DBHelper db);
    }

    private final ExecutorService pool;
    private final DBHelper db;
    public DBRepo(DBHelper db, ExecutorService pool) {
        this.pool = pool;
        this.db = db;
    }

    public void run(RepoRunnable task) {
        pool.execute(() ->
            task.run(db)
        );
    }
}
