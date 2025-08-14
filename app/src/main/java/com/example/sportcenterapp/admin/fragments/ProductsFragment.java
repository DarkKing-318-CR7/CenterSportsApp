package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.ProductAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {

    private RecyclerView rv;
    private ProductAdapter adapter;
    private DatabaseHelper db;
    private final List<Product> data = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_admin_products, parent, false);

        rv = v.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = new DatabaseHelper(requireContext());

        // ADMIN dùng ctor KHÔNG callback -> không còn ClassCastException
        adapter = new ProductAdapter(data, requireContext());
        rv.setAdapter(adapter);

        loadData();
        return v;
    }

    private void loadData() {
        data.clear();
        // dùng API đọc danh sách sản phẩm của bạn
        data.addAll(db.getAllProducts()); // hoặc db.getProducts() tùy tên hàm hiện có
        adapter.notifyDataSetChanged();   // nhớ đúng tên (trước đó bạn gõ thiếu)
    }
}
