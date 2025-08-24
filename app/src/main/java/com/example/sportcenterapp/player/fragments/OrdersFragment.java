// player/fragments/OrdersFragment.java
package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.OrdersAdapter;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.*;
import retrofit2.*;

public class OrdersFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvEmpty;
    private OrdersAdapter adapter;
    private final List<ApiService.OrderDTO> data = new ArrayList<>();

    private ApiService api;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_orders, c, false);
        rv = v.findViewById(R.id.rvOrders);
        tvEmpty = v.findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(data, order -> showDetail(order));
        rv.setAdapter(adapter);

        api = ApiClient.get().create(ApiService.class);
        session = new SessionManager(requireContext());

        loadOrders();
        return v;
    }

    private void loadOrders() {
        int uid = session.getUserId();
        if (uid <= 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        api.getOrdersByUser(uid).enqueue(new Callback<List<ApiService.OrderDTO>>() {
            @Override
            public void onResponse(Call<List<ApiService.OrderDTO>> call, Response<List<ApiService.OrderDTO>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    data.clear();
                    data.addAll(r.body());
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Không tải được đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.OrderDTO>> call, Throwable t) {
                if (!isAdded()) return;
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetail(ApiService.OrderDTO o) {
        // Nếu OrderDetailBottomSheet nhận id:
        OrderDetailBottomSheet.newInstance(o.id)
                .show(getChildFragmentManager(), "order_detail");
    }
}
