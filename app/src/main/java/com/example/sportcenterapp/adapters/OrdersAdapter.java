package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.ApiService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {

    public interface OnClick { void onClick(ApiService.OrderDTO o); }

    private final List<ApiService.OrderDTO> ds;
    private final OnClick cb;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));

    public OrdersAdapter(List<ApiService.OrderDTO> ds, OnClick cb) {
        this.ds = ds;
        this.cb = cb;
    }

    /** total_price có thể là String hoặc Number -> ép an toàn */
    private static double toDouble(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (Exception e) { return 0; }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvStatus, tvTotal, tvTime;
        VH(@NonNull View v) {
            super(v);
            tvId     = v.findViewById(R.id.tvId);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvTotal  = v.findViewById(R.id.tvTotal);
            tvTime   = v.findViewById(R.id.tvTime);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ApiService.OrderDTO o = ds.get(pos);

        h.tvId.setText("#" + o.id);
        h.tvStatus.setText(o.status != null ? o.status : "PENDING");

        double total = toDouble(o.total);
        h.tvTotal.setText(nf.format(Math.round(total)) + "đ");

        h.tvTime.setText(o.created_at != null ? o.created_at : "");

        h.itemView.setOnClickListener(v -> { if (cb != null) cb.onClick(o); });
    }

    @Override public int getItemCount() { return ds.size(); }
}
