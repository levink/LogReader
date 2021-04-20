package com.logger.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.logger.AppService;
import com.logger.R;

public class UrlFragment extends BaseFragment {

    private SearchView searchView;
    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_url, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbar(view);
        setListView(view);
    }
    private void setToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolBar);
        AppCompatActivity activity = (AppCompatActivity)requireActivity();
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        searchView = view.findViewById(R.id.searchView);
    }
    private void setListView(@NonNull View view) {
        String[] files = new String[] {
            "https://snowrider.pro:1306/Hello.html",
            "https://snowrider.pro:1306/test_20mb.txt"
        };
        adapter = new ArrayAdapter<>(requireContext(), R.layout.item_list, files);
        ListView list = view.findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, view1, i, l) -> {
            String url = adapter.getItem(i);
            openMaskFragment(url);
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.url_menu, menu);
        menu.findItem(R.id.search).setOnMenuItemClickListener(menuItem -> {
            String url = searchView.getQuery().toString();
            openMaskFragment(url);
            return true;
        });
        menu.findItem(R.id.close_app).setOnMenuItemClickListener(menuItem -> {
            requireActivity().finish();
            return true;
        });
    }

    private void openMaskFragment(String url) {

        MaskFragment.open(this, url);
//
//        getLifecycle().addObserver(new UrlCheckReceiver(AppService.URL_CHECK_BROADCAST, this));
//        Context context = requireContext();
//        AppService.checkUrl(context, url);
    }

    private static class UrlCheckReceiver extends BroadcastReceiver implements LifecycleObserver {
        private final BaseFragment fragment;
        private final LocalBroadcastManager broadcastManager;
        private final IntentFilter filter;

        public UrlCheckReceiver(String action, BaseFragment fragment) {
            this.fragment = fragment;
            broadcastManager = LocalBroadcastManager.getInstance(fragment.requireContext());
            filter = new IntentFilter(action);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        @SuppressWarnings("unused")
        public void subscribe() {
            broadcastManager.registerReceiver(this, filter);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        @SuppressWarnings("unused")
        public void unsubscribe() {
            broadcastManager.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean canDownload = intent.getBooleanExtra(AppService.Tag.IS_AVAILABLE, false);
            if (canDownload) {
                String url = intent.getStringExtra(AppService.Tag.URL);
                MaskFragment.open(fragment, url);
            } else {
                fragment.toast(R.string.url_not_available);
            }
        }
    }
}
