package com.example.sportcenterapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.fragments.AccountFragment;
import com.example.sportcenterapp.fragments.CourtsFragment;
import com.example.sportcenterapp.fragments.HomeFragment;
import com.example.sportcenterapp.fragments.OrdersFragment;
import com.example.sportcenterapp.fragments.ShopFragment;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        loadFragment(new HomeFragment());

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_user_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_user_courts) {
                    selectedFragment = new CourtsFragment();
                }
//                else if (id == R.id.nav_user_shop)
//                {
//                    selectedFragment = new ShopFragment();
//                }

                else if (id == R.id.nav_user_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (id == R.id.nav_user_account) {
                    selectedFragment = new AccountFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameContainer, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}
