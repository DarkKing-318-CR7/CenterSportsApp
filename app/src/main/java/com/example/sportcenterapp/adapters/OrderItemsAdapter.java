package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.OrderItem;

import java.util.List;
import java.util.Locale;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {

    private final List<OrderItem> data;

    public OrderItemsAdapter(List<OrderItem> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_line, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OrderItem it = data.get(pos);
        h.tvName.setText(it.name);
        h.tvQty.setText(String.format(Locale.getDefault(), "x%d", it.qty));
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,dđ", it.price));
        h.tvLineTotal.setText(String.format(Locale.getDefault(), "%,dđ", it.price * it.qty));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvPrice, tvLineTotal;
        VH(@NonNull View item) {
            super(item);
            tvName = item.findViewById(R.id.tvName);
            tvQty = item.findViewById(R.id.tvQty);
            tvPrice = item.findViewById(R.id.tvPrice);
            tvLineTotal = item.findViewById(R.id.tvLineTotal);
        }
    }
}
