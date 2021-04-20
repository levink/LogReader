package com.logger;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.logger.db.DBHelper;
import com.logger.fragment.IParent;

public class AppActivity extends AppCompatActivity implements IParent {
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
