package com.logger.classes.input;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class SearchViewListener implements SearchView.OnQueryTextListener, LifecycleObserver {

    @FunctionalInterface
    public interface OnChangeCallback {
        void onChange(String newText);
    }

    private final SearchView view;
    private final OnChangeCallback callback;

    public SearchViewListener(OnChangeCallback callback, SearchView view) {
        this.callback = callback;
        this.view = view;
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connect() {
        view.setOnQueryTextListener(this);
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnect() {
        view.setOnQueryTextListener(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (callback != null) {
            callback.onChange(newText);
        }
        return true;
    }
}
