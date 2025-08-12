package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Order;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {
    public interface OnOrderClick { void onClick(int orderId); }
    private final List<Order> data; private final OnOrderClick onClick;
    public OrdersAdapter(List<Order> data, OnOrderClick onClick){ this.data=data; this.onClick=onClick; }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Order o = data.get(pos);
        h.tvId.setText("#"+o.id);
        h.tvTotal.setText(o.getTotalFormatted());
        h.tvTime.setText(o.createdAt);
        h.itemView.setOnClickListener(v -> onClick.onClick(o.id));
    }
    @Override public int getItemCount(){ return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvTotal, tvTime;
        VH(@NonNull View item){ super(item);
            tvId=item.findViewById(R.id.tvOrderId);
            tvTotal=item.findViewById(R.id.tvOrderTotal);
            tvTime=item.findViewById(R.id.tvOrderTime);
        }
    }
}
