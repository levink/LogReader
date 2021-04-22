package com.logger.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.logger.R;

public abstract class BaseFragment extends Fragment {
    private IParentActivity parent;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        parent = (IParentActivity)requireActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }

    protected void show(@IdRes int id) {
        Activity activity = requireActivity();
        View view = activity.findViewById(id);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void hide(@IdRes int id) {
        View view = requireActivity().findViewById(id);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public void navigate(@IdRes int resId, Bundle bundle) {
        if (parent == null) {
            return;
        }

        NavController controller = parent.getNavController();
        if (controller != null) {
            controller.navigate(resId, bundle);
        }
    }

    public void navigateUp() {
        if (parent == null) {
            return;
        }
        NavController controller = parent.getNavController();
        if (controller != null) {
            controller.navigateUp();
        }
    }

    public void navigateRoot() {
        if (parent == null) {
            return;
        }
        NavController controller = parent.getNavController();
        if (controller != null) {
            controller.popBackStack(R.id.urlFragment, false);
        }
    }

    public void toast(@StringRes int textId) {
        Activity activity = requireActivity();
        Toast toast = Toast.makeText(activity, textId, Toast.LENGTH_SHORT);
        toast.show();
    }
}
