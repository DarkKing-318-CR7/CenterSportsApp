package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.adapters.OrderItemsAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class OrderDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ORDER_ID = "order_id";

    public static OrderDetailBottomSheet newInstance(int orderId) {
        OrderDetailBottomSheet s = new OrderDetailBottomSheet();
        Bundle b = new Bundle();
        b.putInt(ARG_ORDER_ID, orderId);
        s.setArguments(b);
        return s;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int orderId = requireArguments().getInt(ARG_ORDER_ID);
        DatabaseHelper db = new DatabaseHelper(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvOrderItems);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new OrderItemsAdapter(db.getOrderItems(orderId)));
    }
}
