package com.example.sportcenterapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.LoginActivity;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.fragments.CourtsFragment;
import com.example.sportcenterapp.admin.fragments.ProductsFragment;
import com.example.sportcenterapp.admin.fragments.UsersFragment;
import com.example.sportcenterapp.admin.fragments.OrdersFragment;   // tạo khung nếu chưa có
import com.example.sportcenterapp.admin.fragments.BookingsFragment; // tạo khung nếu chưa có
import com.example.sportcenterapp.admin.fragments.InventoryFragment; // Tab Sản phẩm | Sân
import com.example.sportcenterapp.player.fragments.ChatSupportFragment;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminActivity extends AppCompatActivity {

        private BottomNavigationView bottomAdmin;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // 1) PHẢI setContentView đúng layout chứa bottomAdmin
            setContentView(R.layout.activity_admin);

            // 2) Tìm đúng id
            bottomAdmin = findViewById(R.id.bottomAdmin);

            // 3) Nếu vẫn null -> layout/id đang sai
            if (bottomAdmin == null) {
                throw new IllegalStateException(
                        "activity_admin.xml không chứa BottomNavigationView với id @id/bottomAdmin");
            }

            // 4) Lắng nghe chọn tab (dùng if/else – R.id không còn final trong AGP 8+)
            bottomAdmin.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_dashboard) {
                    // TODO: thay bằng AdminDashboardFragment khi có
                    switchTo(new OrdersFragment());
                    return true;
                } else if (id == R.id.menu_orders) {
                    switchTo(new OrdersFragment());
                    return true;
                } else if (id == R.id.menu_bookings) {
                    switchTo(new BookingsFragment());
                    return true;
                } else if (id == R.id.menu_inventory) {
                    switchTo(new InventoryFragment());
                    return true;
                } else if (id == R.id.menu_users) {
                    switchTo(new UsersFragment());
                    return true;
                }
                return false;
            });

            // 5) Chọn mặc định
            if (savedInstanceState == null) {
                bottomAdmin.setSelectedItemId(R.id.menu_dashboard);
            }
        }

        private void switchTo(@NonNull Fragment f) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_container, f)
                    .commit();
        }

    // AppBar actions (search + chat)
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // TODO: mở SearchActivity hoặc SearchDialogFragment
            Toast.makeText(this, "Tìm kiếm (demo)", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_chat) {
            // Dùng lại ChatSupportFragment nếu bạn đã có
            switchTo(new ChatSupportFragment());
            return true;

        } else if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn chắc chắn muốn đăng xuất?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> doLogout())
                .show();
    }

    private void doLogout() {
        try {
            // Nếu bạn đã có SessionManager:
            SessionManager sm = new SessionManager(this);
            sm.logout(); // hoặc sm.clearSession()

        } catch (Exception ignored) {
            // fallback nếu SessionManager chưa có hàm logout
        }

        // Điều hướng về màn đăng nhập
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
