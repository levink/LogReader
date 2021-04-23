package com.logger.fragment;

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

import com.logger.AppViewModel;
import com.logger.R;
import com.logger.classes.input.SearchViewListener;

import java.util.Objects;

public class UrlFragment extends BaseFragment {

    private AppViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_url, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.url_menu, menu);
        menu.findItem(R.id.search).setOnMenuItemClickListener(menuItem -> {
            String url = viewModel.getUrl().getValue();
            MaskFragment.open(this, url);
            return true;
        });
        menu.findItem(R.id.close_app).setOnMenuItemClickListener(menuItem -> {
            requireActivity().finish();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = AppViewModel.getInstance(requireActivity());
        setToolbar(view);
        setSearchView(view);
        setListView(view);
    }
    private void setToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolBar);
        AppCompatActivity activity = (AppCompatActivity)requireActivity();
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }
    private void setSearchView(@NonNull View view) {
        SearchView searchView = view.findViewById(R.id.searchView);
        SearchViewListener searchListener = new SearchViewListener(
                newText -> viewModel.setUrl(newText),
                searchView
        );
        getLifecycle().addObserver(searchListener);
        viewModel.getUrl().observe(getViewLifecycleOwner(), url -> {
            String url_old = searchView.getQuery().toString();
            boolean different = !Objects.equals(url, url_old);
            if (different) {
                searchView.setQuery(url, false);
            }
        });
    }
    private void setListView(@NonNull View view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_list);
        ListView list = view.findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, v, i, l) -> {
            String url = (String)adapterView.getItemAtPosition(i);
            MaskFragment.open(this, url);
        });
        viewModel.getUrlHistory().observe(getViewLifecycleOwner(), items -> {
            if (items.isEmpty()) {
                show(R.id.url_history_is_empty);
                list.setVisibility(View.GONE);
            } else {
                hide(R.id.url_history_is_empty);
                adapter.clear();
                adapter.addAll(items);
                list.setVisibility(View.VISIBLE);
            }
        });
    }
}
