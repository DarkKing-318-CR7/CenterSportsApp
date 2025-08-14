package com.example.sportcenterapp.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.fragments.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin);

        BottomNavigationView bottom = findViewById(R.id.bottomAdmin);
        bottom.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.menu_users)        f = new UsersFragment();
            else if (id == R.id.menu_courts)  f = new com.example.sportcenterapp.admin.fragments.CourtsFragment();
            else if (id == R.id.menu_products)f = new com.example.sportcenterapp.admin.fragments.ProductsFragment();
            else f = new UsersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminContainer, f)
                    .commit();
            return true;
        });
        bottom.setSelectedItemId(R.id.menu_users);
    }
}
