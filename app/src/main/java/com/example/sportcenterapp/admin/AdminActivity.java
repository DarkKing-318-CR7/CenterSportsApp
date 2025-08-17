package com.example.sportcenterapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.LoginActivity;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.fragments.AdminDashboardFragment;
import com.example.sportcenterapp.admin.fragments.BookingsFragment;
import com.example.sportcenterapp.admin.fragments.InventoryFragment;
import com.example.sportcenterapp.admin.fragments.OrdersFragment;
import com.example.sportcenterapp.admin.fragments.UsersFragment;
import com.example.sportcenterapp.player.fragments.ChatSupportFragment;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout phải có: toolbarAdmin, bottomAdmin, và FrameLayout @id/admin_container
        setContentView(R.layout.activity_admin);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        bottomAdmin = findViewById(R.id.bottomAdmin);

        if (bottomAdmin == null) {
            Toast.makeText(this, "Thiếu BottomNavigationView (@id/bottomAdmin)", Toast.LENGTH_LONG).show();
            // Fallback: vẫn hiển thị dashboard để không trắng màn hình
            switchTo(new AdminDashboardFragment());
        } else {
            bottomAdmin.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_dashboard) { switchTo(new AdminDashboardFragment()); return true; }
                if (id == R.id.menu_orders)    { switchTo(new OrdersFragment());        return true; }
                if (id == R.id.menu_bookings)  { switchTo(new BookingsFragment());      return true; }
                if (id == R.id.menu_inventory) { switchTo(new InventoryFragment());     return true; }
                if (id == R.id.menu_users)     { switchTo(new UsersFragment());         return true; }
                return false;
            });
        }
        if (savedInstanceState == null) {
            if (bottomAdmin != null) bottomAdmin.setSelectedItemId(R.id.menu_dashboard);
            else switchTo(new AdminDashboardFragment());
        }


        // Menu trên TopAppBar
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                Toast.makeText(this, "Tìm kiếm (demo)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_chat) {
                switchTo(new ChatSupportFragment());
                return true;
            } else if (id == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });

        // Điều hướng bottom nav
        bottomAdmin.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_dashboard) {
                // Tổng quan (2 list mini: Đơn hàng & Đặt sân)
                switchTo(new AdminDashboardFragment());
                return true;
            } else if (id == R.id.menu_orders) {
                // Đơn hàng (duyệt/hủy)
                switchTo(new OrdersFragment());
                return true;
            } else if (id == R.id.menu_bookings) {
                // Đặt sân (danh sách booking đầy đủ)
                switchTo(new BookingsFragment());
                return true;
            } else if (id == R.id.menu_inventory) {
                // Kho & Sân (gộp Products/Courts)
                switchTo(new InventoryFragment());
                return true;
            } else if (id == R.id.menu_users) {
                // Người dùng
                switchTo(new UsersFragment());
                return true;
            }
            return false;
        });

        // Chọn mặc định lần đầu vào
        if (savedInstanceState == null) {
            bottomAdmin.setSelectedItemId(R.id.menu_dashboard);
        }
    }

    private void switchTo(@NonNull Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_container, f)
                .commit();
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
            new SessionManager(this).logout();
        } catch (Exception ignored) { }
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
