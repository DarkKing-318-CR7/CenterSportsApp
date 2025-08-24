package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.adapters.AdminOrdersAdapter;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment implements AdminOrdersAdapter.OnAction {

    private ChipGroup chipGroup;
    private RecyclerView rv;
    private TextView tvEmpty;
    private ProgressBar progress;

    private final List<ApiService.OrderAdminDTO> data = new ArrayList<>();
    private AdminOrdersAdapter adapter;

    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_admin_orders, c, false);

        chipGroup = v.findViewById(R.id.chipGroupOrders);
        rv        = v.findViewById(R.id.rvOrders);
        tvEmpty   = v.findViewById(R.id.tvEmpty);
        progress  = v.findViewById(R.id.progress);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminOrdersAdapter(this);
        rv.setAdapter(adapter);

        api = ApiClient.build().create(ApiService.class);

        // filter with ChipGroup
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String status = "ALL";
            if (!checkedIds.isEmpty()) {
                View chip = group.findViewById(checkedIds.get(0));
                Object tag = chip.getTag();
                if (tag != null) status = tag.toString();
            }
            loadOrders(status);
        });

        // mặc định chipAll được check → gọi lần đầu
        loadOrders("ALL");
        return v;
    }

    private void setLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void loadOrders(String status){
        setLoading(true);
        String s = "ALL".equalsIgnoreCase(status) ? "" : status;
        api.getOrdersAdmin(status).enqueue(new Callback<List<ApiService.OrderAdminDTO>>() {
            @Override
            public void onResponse(Call<List<ApiService.OrderAdminDTO>> call,
                                   Response<List<ApiService.OrderAdminDTO>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body()!=null){
                    data.clear();
                    data.addAll(r.body());
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(data.isEmpty()? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(),"Không tải được đơn hàng",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<ApiService.OrderAdminDTO>> call, Throwable t) {
                if (!isAdded()) return;
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(),"Lỗi mạng: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    // ===== AdminOrdersAdapter.OnAction =====
    @Override public void onClick(ApiService.OrderAdminDTO o) {
        AdminOrderDetailBottomSheet.newInstance(o.id)
                .show(getChildFragmentManager(), "admin_order_detail");
    }

    @Override public void onApprove(ApiService.OrderAdminDTO o) {
        updateStatus(o.id, "APPROVED");
    }

    @Override public void onCancel(ApiService.OrderAdminDTO o) {
        updateStatus(o.id, "CANCELLED");
    }

    private void updateStatus(int orderId, String status){
        // Dùng DTO rõ ràng
        ApiService.OrderStatusUpdateReq req = new ApiService.OrderStatusUpdateReq();
        req.order_id = orderId;
        req.status = status;

        api.updateOrderStatus(req).enqueue(new Callback<ApiService.SimpleResp>() {
            @Override public void onResponse(Call<ApiService.SimpleResp> call, Response<ApiService.SimpleResp> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body()!=null && r.body().ok){
                    Toast.makeText(getContext(),"Đã cập nhật: " + status, Toast.LENGTH_SHORT).show();
                    // reload theo filter hiện tại
                    String cur = "ALL";
                    int checkedId = chipGroup.getCheckedChipId();
                    if (checkedId != View.NO_ID) {
                        Object tag = chipGroup.findViewById(checkedId).getTag();
                        if (tag != null) cur = tag.toString();
                    }
                    loadOrders(cur);
                } else {
                    Toast.makeText(getContext(),"Cập nhật trạng thái thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ApiService.SimpleResp> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),"Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
