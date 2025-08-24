// app/src/main/java/com/example/sportcenterapp/admin/fragments/AdminOrderDetailBottomSheet.java
package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ORDER_ID = "order_id";

    public static AdminOrderDetailBottomSheet newInstance(int orderId){
        Bundle b = new Bundle();
        b.putInt(ARG_ORDER_ID, orderId);
        AdminOrderDetailBottomSheet f = new AdminOrderDetailBottomSheet();
        f.setArguments(b);
        return f;
    }

    private TextView tvOrderCode, tvOrderTotal;
    private RecyclerView rvOrderItems;
    private final ItemsAdapter itemsAdapter = new ItemsAdapter();
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));

    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.sheet_order_detail, c, false);

        tvOrderCode  = v.findViewById(R.id.tvOrderCode);
        tvOrderTotal = v.findViewById(R.id.tvOrderTotal);
        rvOrderItems = v.findViewById(R.id.rvOrderItems);

        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(itemsAdapter);

        api = ApiClient.build().create(ApiService.class);

        int orderId = getArguments() != null ? getArguments().getInt(ARG_ORDER_ID, -1) : -1;
        if (orderId > 0) {
            tvOrderCode.setText("#" + orderId);
            loadItems(orderId);
        }

        return v;
    }

    private void loadItems(int orderId){
        // ĐỔI OrderItem nếu ApiService của bạn dùng tên khác
        api.getOrderItems(orderId).enqueue(new Callback<List<ApiService.OrderItem>>() {
            @Override public void onResponse(Call<List<ApiService.OrderItem>> c,
                                             Response<List<ApiService.OrderItem>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body()!=null){
                    itemsAdapter.setData(r.body());
                    long total = 0;
                    for (ApiService.OrderItem it : r.body()) {
                        long price = Math.round(toDouble(it.price));
                        int qty    = Math.max(1, it.qty);
                        total += price * qty;
                    }
                    tvOrderTotal.setText(nf.format(total) + "đ");
                } else {
                    itemsAdapter.setData(new ArrayList<>());
                    tvOrderTotal.setText("0đ");
                }
            }
            @Override public void onFailure(Call<List<ApiService.OrderItem>> c, Throwable t) {
                if (!isAdded()) return;
                itemsAdapter.setData(new ArrayList<>());
                tvOrderTotal.setText("0đ");
            }
        });
    }

    private static double toDouble(Object v){
        if (v == null) return 0;
        if (v instanceof Number) return ((Number)v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e){ return 0; }
    }

    /** Adapter hiển thị từng dòng trong rvOrderItems (dùng item_order_item.xml) */
    private static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.VH> {
        private final List<ApiService.OrderItem> ds = new ArrayList<>();
        private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));

        void setData(List<ApiService.OrderItem> items){
            ds.clear();
            if (items != null) ds.addAll(items);
            notifyDataSetChanged();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvQty, tvPrice;
            VH(@NonNull View v){
                super(v);
                tvName  = v.findViewById(R.id.tvName);
                tvQty   = v.findViewById(R.id.tvQty);
                tvPrice = v.findViewById(R.id.tvPrice);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_order_item, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            ApiService.OrderItem it = ds.get(pos);
            h.tvName.setText(it.name != null ? it.name : "");
            int qty = Math.max(1, it.qty);
            h.tvQty.setText("x" + qty);
            long price = Math.round(toDouble(it.price));
            h.tvPrice.setText(nf.format(price) + "đ");
        }

        @Override public int getItemCount() { return ds.size(); }
    }
}
