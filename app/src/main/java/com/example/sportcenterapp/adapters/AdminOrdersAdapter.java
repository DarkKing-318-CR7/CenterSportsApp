package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Order;

import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onApprove(int orderId);
        void onReject(int orderId);
    }

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public AdminOrdersAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order o = orders.get(position);
        holder.tvCourtName.setText(o.getCourtName());
        holder.tvTimeSlot.setText("Khung giờ: " + o.getTimeSlot());
        holder.tvStatus.setText("Trạng thái: " + o.getStatus());

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(o.getId());
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(o.getId());
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvTimeSlot, tvStatus;
        Button btnApprove, btnReject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
