package com.logger.fragment;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.logger.App;
import com.logger.AppActivity;
import com.logger.R;
import com.logger.classes.LogWriter;
import com.logger.model.ResultItem;
import com.logger.task.DownloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultFragment extends BaseFragment {

    private static class Model {
        public String url;
        public String mask;
    }

    public static void open(BaseFragment source, String url, String mask) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("mask", mask);
        source.navigate(R.id.resultFragment, bundle);
    }
    public static Model getModel(Bundle bundle) {
        Model model = new Model();
        model.url = bundle.getString("url");
        model.mask = bundle.getString("mask");
        return model;
    }

    private ProgressBar progressBar;
    private ResultAdapter adapter;
    private boolean selectAll;
    private TextView listHeader;
    private DownloadTask task;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbar(view);
        setProgressBar(view);
        setListView(view);
        selectAll = true;

        Bundle bundle = getArguments();
        if (bundle == null) {
            navigateUp();
            return;
        }
        Model model = getModel(bundle);

        if (task == null || task.isComplete()) {
            task = DownloadTask.getOrCreate(model.url, model.mask);
            task.execute();
        }

        progressBar.setProgress(task.getProgress());
        hide(R.id.notFoundTextView);
        task.addListener(new DownloadTask.Listener() {
            @Override
            public void onMatch(List<String> items) {
                if (items.isEmpty()){
                    return;
                }

                ArrayList<ResultItem> result = new ArrayList<>(items.size());
                for(String item : items) {
                    result.add(new ResultItem(item, false));
                }

                requireActivity().runOnUiThread(() -> {
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onProgress(int progress) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                });
            }
            @Override
            public void onComplete() {
                requireActivity().runOnUiThread(() ->{
                    if (adapter.isEmpty()) {
                        show(R.id.notFoundTextView);
                    } else {
                        hide(R.id.notFoundTextView);
                    }
                });
            }
            @Override
            public void onCancel() { }
            @Override
            public void onFail() { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        task.removeListener();
    }

    private void setToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolBar);
        AppActivity activity = (AppActivity)requireActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
    }
    private void setProgressBar(@NonNull View view) {
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
    }
    private void setListView(@NonNull View view) {
        adapter = new ResultAdapter(requireContext());
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, clickedView, position, id) -> {
            if (listHeader != null) {
                position--;
            }
            ResultItem item = adapter.getItem(position);
            item.checked = !item.checked;
            adapter.notifyDataSetChanged();
        });
        listHeader = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.result_menu, menu);
        menu.findItem(R.id.from_the_begin).setOnMenuItemClickListener(menuItem -> {
            navigateRoot();
            return true;
        });
        menu.findItem(R.id.select_all).setOnMenuItemClickListener(menuItem -> {
            adapter.markAll(selectAll);
            selectAll = !selectAll;
            return true;
        });
        menu.findItem(R.id.copy).setOnMenuItemClickListener(menuItem -> {
            CharSequence text = adapter.getSelectedText();
            copy(text);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (task != null) {
                task.cancel();
            }
            navigateUp();
            return true;
        }
        return false;
    }

    private void copy(CharSequence text) {
        if (text.length() == 0) {
            toast(R.string.nothing_to_copy);
            return;
        }

        ClipboardManager clipBoard = (ClipboardManager)(requireActivity().getSystemService(Context.CLIPBOARD_SERVICE));
        if (clipBoard == null) {
            toast(R.string.copy_failed);
            return;
        }

        ClipData data = ClipData.newPlainText("text", text);
        clipBoard.setPrimaryClip(data);
        toast(R.string.copy_success);
    }

    static class ResultAdapter extends ArrayAdapter<ResultItem> {

        private final LayoutInflater inflater;

        public ResultAdapter(@NonNull Context context) {
            super(context, 0);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ResultItem item = getItem(position);
            if (view == null) {
                view = inflater.inflate(R.layout.item_result, viewGroup, false);
            }

            CheckedTextView tv = view.findViewById(R.id.resultText);
            tv.setText(item.value);
            tv.setChecked(item.checked);
            return view;
        }

        public void markAll(boolean checked) {
            int count = getCount();
            for(int i = 0; i < count; i++) {
                ResultItem item = getItem(i);
                item.checked = checked;
            }
            notifyDataSetChanged();
        }

        public CharSequence getSelectedText() {
            StringBuilder sb = new StringBuilder();
            int count = getCount();
            for(int i = 0; i < count; i++) {
                ResultItem item = getItem(i);
                if (item.checked) {
                    sb.append(item.value).append("\n");
                }
            }
            return sb;
        }
    }
}
