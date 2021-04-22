package com.logger.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.logger.AppActivity;
import com.logger.AppViewModel;
import com.logger.R;
import com.logger.model.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ParseFragment extends BaseFragment {

    private static class Model {
        String url;
        String mask;
        boolean isCorrect() {
            return url != null && mask != null;
        }
    }

    public static void open(BaseFragment source, String url, String mask) {
        if (url == null || mask == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("mask", mask);
        source.navigate(R.id.parseFragment, bundle);
    }
    private static Model getModel(Bundle bundle) {
        Model model = new Model();
        if (bundle == null) {
            return model;
        }

        model.url = bundle.getString("url");
        model.mask = bundle.getString("mask");
        return model;
    }

    private AppViewModel viewModel;
    private ProgressBar progressBar;
    private MatchAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.result_menu, menu);
        menu.findItem(R.id.from_the_begin).setOnMenuItemClickListener(menuItem -> {
            viewModel.setMask(null);
            viewModel.setUrl(null);
            viewModel.cancelSearch();
            navigateRoot();
            return true;
        });
        menu.findItem(R.id.select_all).setOnMenuItemClickListener(menuItem -> {
            viewModel.switchSelectAll();
            return true;
        });
        menu.findItem(R.id.copy).setOnMenuItemClickListener(menuItem -> {
            CharSequence text = adapter.getSelectedText();
            copy(text);
            return true;
        });
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            viewModel.cancelSearch();
            navigateUp();
            return true;
        }
        return false;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = AppViewModel.getInstance(requireActivity());

        setToolbar(view);
        setProgressBar(view);
        setListView(view);

        if (savedInstanceState == null) {
            Model model = getModel(getArguments());
            if (model.isCorrect()) {
                viewModel.startSearch(model.url, model.mask);
            } else {
                navigateRoot();
            }
        } else {
            viewModel.resumeSearch();
        }
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
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress ->
            progressBar.setProgress(progress)
        );
    }
    private void setListView(@NonNull View view) {
        adapter = new MatchAdapter(requireContext());
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, v, i, id) -> {
            Match item = (Match)adapterView.getItemAtPosition(i);
            item.checked = !item.checked;
            adapter.notifyDataSetChanged();
        });
        View notFoundText = view.findViewById(R.id.notFoundTextView);
        viewModel.getMatchesQueue().observe(getViewLifecycleOwner(), queue -> {
            if (queue.isEmpty()) {
                return;
            }
            adapter.drainFrom(queue);
            adapter.notifyDataSetChanged();
            notFoundText.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSelectAll().observe(getViewLifecycleOwner(), checked ->
            adapter.markAll(checked)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.pauseSearch();
    }

    static class MatchAdapter extends ArrayAdapter<Match> {

        private final LayoutInflater inflater;
        private final List<Match> items = new ArrayList<>();

        public MatchAdapter(@NonNull Context context) {
            super(context, 0);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Nullable
        @Override
        public Match getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void drainFrom(LinkedBlockingQueue<Match> queue) {
            queue.drainTo(items);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            Match item = getItem(position);
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
                Match item = getItem(i);
                item.checked = checked;
            }
            notifyDataSetChanged();
        }

        public CharSequence getSelectedText() {
            StringBuilder sb = new StringBuilder();
            int count = getCount();
            for(int i = 0; i < count; i++) {
                Match item = getItem(i);
                if (item.checked) {
                    sb.append(item.value).append("\n");
                }
            }
            return sb;
        }
    }
}
