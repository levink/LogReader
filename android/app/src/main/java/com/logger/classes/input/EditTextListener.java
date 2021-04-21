package com.logger.classes.input;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.logger.fragment.MaskFragment;

public class EditTextListener implements TextWatcher, LifecycleObserver {

    @FunctionalInterface
    public interface OnChangeCallback {
        void onChange(String newText);
    }

    private final OnChangeCallback callback;
    private final EditText editText;

    public EditTextListener(OnChangeCallback callback, EditText editText) {
        this.callback = callback;
        this.editText = editText;
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connect() {
        editText.addTextChangedListener(this);
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnect() {
        editText.removeTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (callback != null) {
            callback.onChange(s.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s) { }
}
