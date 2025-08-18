package com.example.sportcenterapp.player;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.player.fragments.AccountFragment;
import com.example.sportcenterapp.player.fragments.BookingFragment;
import com.example.sportcenterapp.player.fragments.BookingHistoryFragment;
import com.example.sportcenterapp.player.fragments.CartFragment;
import com.example.sportcenterapp.player.fragments.ChatSupportFragment;
import com.example.sportcenterapp.player.fragments.CoachFragment;
import com.example.sportcenterapp.player.fragments.HomeFragment;
import com.example.sportcenterapp.player.fragments.OrdersFragment;
import com.example.sportcenterapp.player.fragments.ShopFragment;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class PlayerActivity extends AppCompatActivity {

    private MaterialToolbar topBar;
    private BottomNavigationView bottom;
    private DrawerLayout drawer; // null nếu layout chưa có Drawer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        com.example.sportcenterapp.utils.PermissionHelper.ensureImagePermission(this);


        topBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topBar);

        // TopAppBar: nút "Giỏ" ở góc phải
//        topBar.inflateMenu(R.menu.menu_player_top);
//        topBar.setOnMenuItemClickListener(this::onTopMenuClick);

        // Nếu layout có DrawerLayout + NavigationView thì bật hamburger & xử lý click
        drawer = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);
        if (drawer != null && navView != null) {
            ActionBarDrawerToggle toggle =
                    new ActionBarDrawerToggle(this, drawer, topBar,
                            R.string.nav_open, R.string.nav_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_booking_history) {
                    replace(new BookingHistoryFragment(), "Lịch sử đặt sân"); // TODO: thay bằng BookingHistoryFragment khi có
                } else if (id == R.id.nav_order_history) {
                    replace(new OrdersFragment(), "Lịch sử mua hàng");  // TODO: thay bằng OrdersFragment khi có
                } else if (id == R.id.nav_support_chat) {
                    replace(new ChatSupportFragment(), "Chat hỗ trợ");       // TODO: thay bằng ChatSupportFragment khi có
                }
                drawer.closeDrawers();
                return true;
            });
        }

        // Bottom Navigation
        bottom = findViewById(R.id.bottomNav);
        bottom.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.tab_booking)       replace(new BookingFragment(), "Đặt sân");
            else if (id == R.id.tab_shop)     replace(new ShopFragment(), "Shop");
            else if (id == R.id.tab_coach)    replace(new CoachFragment(), "Thuê HLV");
            else if (id == R.id.tab_account)  replace(new AccountFragment(), "Tài khoản");
            else                               replace(new HomeFragment(), "Trang chủ");
            return true;
        });

        if (savedInstanceState == null) {
            // mở mặc định tab Home
            if (bottom != null) bottom.setSelectedItemId(R.id.tab_home);
            else replace(new HomeFragment(), "Trang chủ");
        }

        // cập nhật số lượng giỏ ban đầu (nếu có)
        refreshCartCount();
    }

    private boolean onTopMenuClick(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            replace(new CartFragment(), "Giỏ hàng");
            return true;
        }
        return false;
    }

    private void replace(@NonNull Fragment f, @NonNull String subtitle) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_nav_host, f)
                .commit();
        setTitleBar(subtitle);
    }

    private void setTitleBar(@NonNull String sub) {
        topBar.setTitle("CenterBooking");
        topBar.setSubtitle(sub);
    }

    /** Cho ShopFragment/CartFragment gọi để cập nhật “Giỏ (n)” trên TopAppBar */
    public void refreshCartCount() {
        int uid = new SessionManager(this).getUserId();
        int count = new DatabaseHelper(this).getCartCount(uid);
        if (topBar != null && topBar.getMenu() != null) {
            MenuItem m = topBar.getMenu().findItem(R.id.action_cart);
            if (m != null) m.setTitle(count > 0 ? "Giỏ (" + count + ")" : "Giỏ");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartCount();
    }
}
