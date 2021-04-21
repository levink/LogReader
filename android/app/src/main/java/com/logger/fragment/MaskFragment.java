package com.logger.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.logger.AppViewModel;
import com.logger.R;
import com.logger.classes.OneLineFilter;

import java.util.Objects;

public class MaskFragment extends BaseFragment {

    public static void open(BaseFragment source, String url) {
        if (url == null) {
            source.toast(R.string.is_empty_url);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        source.navigate(R.id.maskFragment, bundle);
    }
    private static String getUrl(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString("url");
    }

    private AppViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mask, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mask_menu, menu);
        MenuItem parseBtn = menu.findItem(R.id.parse);
        parseBtn.setOnMenuItemClickListener(menuItem -> {
            String url = viewModel.getUrl().getValue();
            String mask = viewModel.getMask().getValue();
            openParseFragment(url, mask);
            return false;
        });
        menu.findItem(R.id.clear_history).setOnMenuItemClickListener(menuItem -> {
            viewModel.clearMaskHistory();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
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
        setSearchView(view);
        setListView(view);

        if (savedInstanceState == null) {
            String url = getUrl(getArguments());
            if (url == null) {
                navigateRoot();
            } else {
                viewModel.setUrl(url);
            }
        }
    }
    private void setToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolBar);
        AppCompatActivity activity = (AppCompatActivity)requireActivity();
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void setSearchView(@NonNull View view) {
        EditText editText = view.findViewById(R.id.editText);
        editText.setFilters(new InputFilter[] { new OneLineFilter() });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setMask(s.toString());
            }
        });
        viewModel.getMask().observe(getViewLifecycleOwner(), mask -> {
            boolean different = !Objects.equals(mask, editText.getText().toString());
            if (different) {
                editText.setText(mask);
            }
        });
    }
    private void setListView(@NonNull View view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_list);
        ListView list = view.findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, v, i, l) -> {
            String url = viewModel.getUrl().getValue();
            String mask = (String) adapterView.getItemAtPosition(i);
            openParseFragment(url, mask);
        });
        viewModel.getMaskHistory().observe(getViewLifecycleOwner(), items -> {
            if (items.isEmpty()) {
                show(R.id.no_masks_in_history);
                list.setVisibility(View.GONE);
            } else {
                hide(R.id.no_masks_in_history);
                adapter.clear();
                adapter.addAll(items);
                list.setVisibility(View.VISIBLE);
            }
        });
    }
    private void openParseFragment(String url, String mask) {
        if (url == null) {
            toast(R.string.is_empty_url);
            return;
        }
        if (mask == null) {
            toast(R.string.is_empty_mask);
            return;
        }
        ParseFragment.open(this, url, mask);
    }
}
