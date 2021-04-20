package com.logger.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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

import com.logger.App;
import com.logger.R;
import com.logger.db.DBHelper;

public class MaskFragment extends BaseFragment {

    public static void open(BaseFragment source, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        source.navigate(R.id.maskFragment, bundle);
    }

    private EditText editText;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mask, container, false);
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
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        editText = toolbar.findViewById(R.id.editText);
        editText.setFilters(new InputFilter[] { new IgnoreBreakLineFilter() });
    }
    private void setListView(@NonNull View view){
        DBHelper db = App.getDB();
        adapter = new ArrayAdapter<>(requireContext(), R.layout.item_list, db.getMasks());
        ListView list = view.findViewById(R.id.listView);
        list.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            hide(R.id.no_masks_in_history);
        }

        list.setOnItemClickListener((adapterView, view1, i, l) -> {
            String mask = adapter.getItem(i);
            checkMask(mask);
        });
    }
    private void checkMask(CharSequence mask) {
        if (mask.length() == 0) {
            toast(R.string.is_empty_mask);
            return;
        }

        Bundle arguments = getArguments();
        if (arguments != null) {
            String url = arguments.getString("url");
            String maskStr = mask.toString();
            App.getDB().save(url, maskStr);
            ResultFragment.open(this, url, maskStr);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mask_menu, menu);
        MenuItem parseBtn = menu.findItem(R.id.parse);
        parseBtn.setOnMenuItemClickListener(menuItem -> {
            checkMask(editText.getText());
            return false;
        });
        menu.findItem(R.id.clear_history).setOnMenuItemClickListener(menuItem -> {
            DBHelper db = App.getDB();
            db.clearMaskHistory();
            adapter.clear();
            show(R.id.no_masks_in_history);
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

    private static class IgnoreBreakLineFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end,
                                   Spanned spanned, int spannedStart, int spannedEnd) {
            for (int i = start; i < end; i++) {
                if (charSequence.charAt(i) == '\n') {
                    return "";
                }
            }
            return null;
        }
    }
}
