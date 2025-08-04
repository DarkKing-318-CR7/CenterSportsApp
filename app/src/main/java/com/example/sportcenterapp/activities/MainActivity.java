package com.example.sportcenterapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.fragments.AccountFragment;
import com.example.sportcenterapp.fragments.CourtsFragment;
import com.example.sportcenterapp.fragments.HomeFragment;
import com.example.sportcenterapp.fragments.OrdersFragment;
import com.example.sportcenterapp.fragments.ShopFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        loadFragment(new HomeFragment());

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_courts) {
                    selectedFragment = new CourtsFragment();
                } else if (id == R.id.nav_shop) {
                    selectedFragment = new ShopFragment();
                } else if (id == R.id.nav_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (id == R.id.nav_account) {
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
