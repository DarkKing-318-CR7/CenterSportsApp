package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.OrdersAdapter;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private OrdersAdapter adapter;
    private ApiService api;
    private SessionManager session;

    // Dùng DTO trực tiếp
    private final List<ApiService.OrderDTO> data = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_orders, container, false);

        RecyclerView rv   = v.findViewById(R.id.rvOrders);
        TextView tvEmpty  = v.findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // !!! Quan trọng: callback gồm 2 tham số (o, position)
        adapter = new OrdersAdapter(data, (o, position) -> showDetail(o));
        rv.setAdapter(adapter);

        api = ApiClient.getInstance().create(ApiService.class);
        session = new SessionManager(requireContext());

        loadOrders(tvEmpty);
        return v;
    }

    private void loadOrders(TextView tvEmpty) {
        int uid = session.getUserId();
        if (uid <= 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            adapter.submit(Collections.emptyList());
            return;
        }

        api.getOrdersByUser(uid).enqueue(new Callback<List<ApiService.OrderDTO>>() {
            @Override
            public void onResponse(Call<List<ApiService.OrderDTO>> call, Response<List<ApiService.OrderDTO>> res) {
                if (!isAdded()) return;
                if (res.isSuccessful() && res.body() != null) {
                    List<ApiService.OrderDTO> list = res.body();
                    adapter.submit(list);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    adapter.submit(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.OrderDTO>> call, Throwable t) {
                if (!isAdded()) return;
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.submit(Collections.emptyList());
            }
        });
    }

    private void showDetail(ApiService.OrderDTO o) {
        // Nếu OrderDetailBottomSheet nhận id:
        OrderDetailBottomSheet.newInstance(o.id)
                .show(getChildFragmentManager(), "order_detail");
    }
}
