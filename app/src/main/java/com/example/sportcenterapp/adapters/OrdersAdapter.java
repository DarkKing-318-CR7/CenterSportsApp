package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.ApiService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {

    @FunctionalInterface
    public interface OnItemClick { void onClick(ApiService.OrderDTO o, int position); }

    private final List<ApiService.OrderDTO> data = new ArrayList<>();
    @Nullable private final OnItemClick onItemClick;

    private final NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));

    public OrdersAdapter(@Nullable List<ApiService.OrderDTO> init, @Nullable OnItemClick onItemClick) {
        if (init != null) data.addAll(init);
        this.onItemClick = onItemClick;
    }

    public void submit(@Nullable List<ApiService.OrderDTO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);   // đúng với layout bạn gửi
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ApiService.OrderDTO o = data.get(pos);

        // id là int -> không check null
        h.tvId.setText("#" + o.id);

        // status
        h.tvStatus.setText(o.status == null ? "" : o.status);

        // totalPrice: có thể là double hoặc String -> format an toàn
        String totalDisplay = "";
        try {
            // TH1: OrderDTO.totalPrice là double
            double total = (double) ApiService.OrderDTO.class.getField("totalPrice").get(o);
            totalDisplay = vnd.format(total);
        } catch (NoSuchFieldException ignore) {
            try {
                // TH2: OrderDTO.totalPrice là String
                String s = (String) ApiService.OrderDTO.class.getField("totalPrice").get(o);
                if (s != null && !s.isEmpty()) {
                    try { totalDisplay = vnd.format(Double.parseDouble(s)); }
                    catch (Exception e) { totalDisplay = s; }
                }
            } catch (Exception ignore2) {
                // Không tìm thấy field -> để trống
            }
        } catch (Exception e) {
            // total
            if (o.totalPrice != null) {
                h.tvTotal.setText(vnd.format(o.totalPrice));
            } else {
                h.tvTotal.setText(""); // hoặc "0 đ"
            }

        }
        h.tvTotal.setText(totalDisplay);

        // created_at
        h.tvTime.setText(o.created_at == null ? "" : o.created_at);

        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(o, pos);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    // public để tránh warning “exposed outside visibility scope”
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView tvId, tvStatus, tvTotal, tvTime;
        public VH(@NonNull View v) {
            super(v);
            tvId     = v.findViewById(R.id.tvId);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvTotal  = v.findViewById(R.id.tvTotal);
            tvTime   = v.findViewById(R.id.tvTime);

            if (tvId == null || tvStatus == null || tvTotal == null || tvTime == null) {
                throw new IllegalStateException("item_order.xml phải có id: tvId, tvStatus, tvTotal, tvTime");
            }
        }
    }
}
