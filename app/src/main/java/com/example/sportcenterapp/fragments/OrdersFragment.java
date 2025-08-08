package com.example.sportcenterapp.fragments;

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
import com.example.sportcenterapp.adapters.OrderAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Order;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.List;

public class OrdersFragment extends Fragment {
    RecyclerView recyclerView;
    OrderAdapter adapter;
    DatabaseHelper db;
    SessionManager session;

    public static OrdersFragment newInstance(int userId) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        recyclerView = view.findViewById(R.id.recyclerOrders);
        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        int userId = session.getUser().getId();
        List<Order> orders = db.getOrdersByUser(userId);

        adapter = new OrderAdapter(getContext(), orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}

