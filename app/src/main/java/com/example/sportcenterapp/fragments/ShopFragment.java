package com.example.sportcenterapp.fragments;
// imports cần có
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.ShopAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.List;

public class ShopFragment extends Fragment {

    RecyclerView recyclerView;
    ShopAdapter adapter;
    DatabaseHelper db;
    SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recyclerShop);
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        List<Product> products = db.getAllProducts();  // <-- tên hàm chuẩn
        adapter = new ShopAdapter(requireContext(), products);

        adapter.setOnAddToCartListener(product -> {
            db.addToCart(session.getUser().getId(), product.getId(), 1);
            Toast.makeText(requireContext(), "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}
