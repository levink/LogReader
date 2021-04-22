package com.logger.classes;

public interface RepoCallback<T> {
    void onComplete(T result);
}
