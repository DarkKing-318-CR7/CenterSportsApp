package com.example.sportcenterapp.fragments;

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
import com.example.sportcenterapp.adapters.AdminOrdersAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Order;

import java.util.List;

public class AdminOrdersFragment extends Fragment {

    DatabaseHelper db;
    RecyclerView recyclerView;
    AdminOrdersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerAdminOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new DatabaseHelper(getContext());

        loadOrders();

        return view;
    }

    private void loadOrders() {
        List<Order> orders = db.getAllCourtOrders();
        adapter = new AdminOrdersAdapter(getContext(), orders, new AdminOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onApprove(int orderId) {
                if (db.approveOrder(orderId)) {
                    Toast.makeText(getContext(), "Đã duyệt đơn", Toast.LENGTH_SHORT).show();
                    loadOrders();
                }
            }

            @Override
            public void onReject(int orderId) {
                if (db.rejectOrder(orderId)) {
                    Toast.makeText(getContext(), "Đã từ chối đơn", Toast.LENGTH_SHORT).show();
                    loadOrders();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
