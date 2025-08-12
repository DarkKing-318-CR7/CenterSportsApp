package com.example.sportcenterapp.player.fragments;

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
import com.example.sportcenterapp.adapters.OrdersAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.utils.SessionManager;

public class OrdersFragment extends Fragment {
    private DatabaseHelper db; private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        RecyclerView rv = v.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new OrdersAdapter(db.getOrders(session.getUserId()), clickOrderId -> {
            // mở OrderDetailBottomSheet hoặc Activity
            OrderDetailBottomSheet.newInstance(clickOrderId)
                    .show(getParentFragmentManager(), "order_detail");

        }));
    }
}
