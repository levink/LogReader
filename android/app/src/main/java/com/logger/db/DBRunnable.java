package com.logger.db;

@FunctionalInterface
public interface DBRunnable {
    void run(DBHelper db);
}
