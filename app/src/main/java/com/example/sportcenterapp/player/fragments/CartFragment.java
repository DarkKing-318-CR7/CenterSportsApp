package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CartAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvTotal;
    private DatabaseHelper db;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_cart, c, false);

        rv = v.findViewById(R.id.rvCart);
        tvTotal = v.findViewById(R.id.tvTotal);
        Button btnCheckout = v.findViewById(R.id.btnCheckout);

        db = new DatabaseHelper(getContext());
        userId = new SessionManager(requireContext()).getUserId();

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setClipToPadding(false);

        // Đổi nhãn: không còn “Thanh toán” trong app
        btnCheckout.setText("Đặt hàng");
        btnCheckout.setOnClickListener(vw -> doCheckout());

        loadCart();
        return v;
    }

    private void loadCart() {
        List<Object[]> cart = db.getCartItems(userId);
        rv.setAdapter(new CartAdapter(cart, (productId, newQty) -> {
            db.updateCartQty(userId, productId, newQty);
            loadCart();
            // cập nhật badge số lượng trên TopAppBar (nếu Activity có)
            if (getActivity() instanceof com.example.sportcenterapp.player.PlayerActivity) {
                ((com.example.sportcenterapp.player.PlayerActivity) getActivity()).refreshCartCount();
            }
        }));
        tvTotal.setText(String.format(Locale.getDefault(), "Tổng: %,.0fđ", db.getCartTotal(userId)));
    }

    /** Tạo Order (status=pending) + chuyển sang lịch sử đơn; KHÔNG thanh toán trong app */
    private void doCheckout() {
        if (db.getCartItems(userId).isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        long orderId;
        try {
            // Ghi đơn vào bảng orders với status mặc định 'pending'
            orderId = db.checkoutFromCart(userId);
        } catch (Throwable ignored) {
            db.checkoutFromCart(userId);
            orderId = 1;
        }

        // Cập nhật UI/badge
        loadCart();
        if (getActivity() instanceof com.example.sportcenterapp.player.PlayerActivity) {
            ((com.example.sportcenterapp.player.PlayerActivity) getActivity()).refreshCartCount();
        }

        // Thông báo kiểu “đặt hàng” – thanh toán tại quầy
        Toast.makeText(getContext(),
                orderId > 0 ? "Đã tạo đơn. Thanh toán tại quầy." : "Đã tạo đơn.",
                Toast.LENGTH_SHORT).show();

        // Điều hướng sang OrdersFragment (lịch sử đơn của người chơi)
        try {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new OrdersFragment())
                    .commit();
            MaterialToolbar tb = requireActivity().findViewById(R.id.topAppBar);
            if (tb != null) tb.setSubtitle("Lịch sử đơn hàng");
        } catch (Exception e) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host,
                            new com.example.sportcenterapp.player.fragments.OrdersFragment())
                    .commit();
        }
    }
}
