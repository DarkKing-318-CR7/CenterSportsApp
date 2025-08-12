package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
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

        loadCart();

        btnCheckout.setOnClickListener(vw -> doCheckout());

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

    /** Tạo Order + chuyển sang OrdersFragment */
    private void doCheckout() {
        if (db.getCartItems(userId).isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu DatabaseHelper.checkout trả về id đơn hàng → dùng; nếu trả void thì coi như thành công.
        long orderId;
        try {
            // ưu tiên hàm trả về id
            orderId = db.checkoutFromCart(userId); // <-- nếu project bạn hiện trả void, đổi sang gọi db.checkout(userId); và set orderId = 1
        } catch (Throwable ignored) {
            // fallback nếu checkout hiện tại là void
            db.checkoutFromCart(userId);
            orderId = 1;
        }

        // cập nhật giao diện/badge
        loadCart();
        if (getActivity() instanceof com.example.sportcenterapp.player.PlayerActivity) {
            ((com.example.sportcenterapp.player.PlayerActivity) getActivity()).refreshCartCount();
        }

        Toast.makeText(getContext(),
                orderId > 0 ? "Thanh toán thành công!" : "Đã xử lý thanh toán",
                Toast.LENGTH_SHORT).show();

        // Điều hướng sang OrdersFragment để xem lịch sử đơn
        try {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new OrdersFragment())
                    .commit();
            // cập nhật subtitle
            MaterialToolbar tb = requireActivity().findViewById(R.id.topAppBar);
            if (tb != null) tb.setSubtitle("Lịch sử đơn hàng");

        } catch (Exception e) {
            // nếu class path khác, dùng fully-qualified class
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host,
                            new com.example.sportcenterapp.player.fragments.OrdersFragment())
                    .commit();
        }
    }
}
