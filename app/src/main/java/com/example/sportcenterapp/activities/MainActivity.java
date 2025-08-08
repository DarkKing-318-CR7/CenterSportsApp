package com.example.sportcenterapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.fragments.AccountFragment;
import com.example.sportcenterapp.fragments.AdminDashboardFragment;
import com.example.sportcenterapp.fragments.AdminOrdersFragment;
import com.example.sportcenterapp.fragments.CourtsFragment;
import com.example.sportcenterapp.fragments.HomeFragment;
import com.example.sportcenterapp.fragments.OrdersFragment;
import com.example.sportcenterapp.fragments.ShopFragment;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private String userRole = "player";
    private int userId = -1;
    private BottomNavigationView bottomNavigation;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        session = new SessionManager(this);

        // Chặn truy cập nếu chưa login
        if (session.getUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        // Lấy user từ session
        userRole = session.getUser().getRole();  // "admin"/"player"/"coach"...
        userId   = session.getUser().getId();

        // (Optional) Ghi đè bởi intent nếu bạn truyền từ LoginActivity
        Intent it = getIntent();
        if (it != null) {
            String r = it.getStringExtra("role");
            int uid  = it.getIntExtra("userId", -1);
            if (r != null && !r.isEmpty()) userRole = r;
            if (uid != -1) userId = uid;
        }

        // Luôn clear trước khi inflate để tránh lẫn menu user/admin
        bottomNavigation.getMenu().clear();
        if ("admin".equalsIgnoreCase(userRole)) {
            bottomNavigation.inflateMenu(R.menu.menu_admin);     // 3 mục: duyệt đơn, thống kê, tài khoản
        } else {
            bottomNavigation.inflateMenu(R.menu.bottom_menu);    // 5 mục: home, courts, shop, orders, account
        }

        // Mở tab mặc định
        if (savedInstanceState == null) {
            if ("admin".equalsIgnoreCase(userRole)) {
                bottomNavigation.setSelectedItemId(R.id.nav_admin_orders);
                loadFragment(new AdminOrdersFragment());
            } else {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                loadFragment(new HomeFragment());
            }
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();

            if ("admin".equalsIgnoreCase(userRole)) {
                if (id == R.id.nav_admin_orders) {
                    f = new AdminOrdersFragment();
                } else if (id == R.id.nav_admin_dashboard) {
                    f = new AdminDashboardFragment();
                } else if (id == R.id.nav_account) {
                    f = AccountFragment.newInstance(userId, userRole);
                }
            } else {
                if (id == R.id.nav_home) {
                    f = new HomeFragment();
                } else if (id == R.id.nav_courts) {
                    f = new CourtsFragment();
                } else if (id == R.id.nav_shop) {
                    f = new ShopFragment();
                } else if (id == R.id.nav_orders) {
                    f = OrdersFragment.newInstance(userId);
                } else if (id == R.id.nav_account) {
                    f = AccountFragment.newInstance(userId, userRole);
                }
            }

            if (f != null) {
                loadFragment(f);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
