package com.logger;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.logger.fragment.IParentActivity;

public class AppActivity extends AppCompatActivity implements IParentActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public NavController getNavController() {
        if (navController == null) {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        }
        return navController;
    }
}
