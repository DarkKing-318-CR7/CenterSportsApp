package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.ProductAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ShopFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_shop, c, false);

        RecyclerView rv = v.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Product> products = db.getProducts();

// Adapter dành cho PLAYER (có nút Thêm)
        ProductAdapter ad = new ProductAdapter(products, requireContext(), p -> {
            int userId = new SessionManager(requireContext()).getUserId();

            try (DatabaseHelper dbh = new DatabaseHelper(requireContext())) {
                dbh.addToCart(userId, p.id, 1);
                int count = dbh.getCartCount(userId);

                Snackbar.make(rv, "Đã thêm \"" + p.name + "\" (Giỏ: " + count + ")", Snackbar.LENGTH_LONG)
                        .setAction("Xem giỏ", view1 -> {
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.player_nav_host, new CartFragment())
                                    .commit();
                        })
                        .show();

                MaterialToolbar tb = requireActivity().findViewById(R.id.topAppBar);
                if (tb != null) tb.setSubtitle("Giỏ hàng: " + count);
            }
        });
        rv.setAdapter(ad);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Chỉ Shop có cart icon
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                menu.clear(); // xóa menu cũ của Activity (nếu có)
                inflater.inflate(R.menu.menu_shop_top, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_cart) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.player_nav_host, new CartFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
}
