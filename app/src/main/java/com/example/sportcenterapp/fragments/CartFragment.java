package com.example.sportcenterapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CartAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.CartItem;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.List;

public class CartFragment extends Fragment {
    RecyclerView recyclerView;
    CartAdapter adapter;
    DatabaseHelper db;
    SessionManager session;
    private void reloadCart() {
        List<CartItem> updatedItems = db.getCartByUserId(session.getUser().getId());
        adapter.setCartItems(updatedItems);
        adapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerCart);
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        // Load giỏ hàng ban đầu
        List<CartItem> items = db.getCartByUserId(session.getUser().getId());
        adapter = new CartAdapter(requireContext(), items);
        recyclerView.setAdapter(adapter);

        adapter.setOnDeleteItemListener(item -> {
            db.deleteCartItem(item.getId());
            Toast.makeText(requireContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
            reloadCart();
        });

        // Nút đặt hàng
        Button btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> {
            boolean ok = db.placeOrder(session.getUser().getId());
            if (ok) {
                Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                reloadCart();
            } else {
                Toast.makeText(requireContext(), "Giỏ trống hoặc lỗi!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
