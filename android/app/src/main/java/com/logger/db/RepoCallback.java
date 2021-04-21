package com.logger.db;

public interface RepoCallback<T> {
    void onComplete(T result);
}
