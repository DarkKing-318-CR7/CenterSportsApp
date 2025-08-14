package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Booking;

import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.H> {
    private final java.util.List<Booking> data;
    public BookingHistoryAdapter(java.util.List<Booking> d) { this.data = d; }

    static class H extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvCourt, tvDate, tvTime, tvPrice, tvStatus;
        H(View v){
            super(v);
            iv = v.findViewById(R.id.ivCourt);
            tvCourt = v.findViewById(R.id.tvCourt);
            tvDate = v.findViewById(R.id.tvDate);
            tvTime = v.findViewById(R.id.tvTime);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }

    @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_booking_history, p, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H h, int i) {
        Context ctx = h.itemView.getContext();
        Booking m = data.get(i);

        h.tvCourt.setText(m.courtName);
        h.tvDate.setText(m.date);
        h.tvTime.setText(m.startTime + " - " + m.endTime);
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", m.totalPrice));

        // status màu nhẹ
        h.tvStatus.setText(m.status == null ? "CONFIRMED" : m.status);
        int color = Color.parseColor("#00897B"); // confirmed
        if ("CANCELLED".equalsIgnoreCase(m.status)) color = Color.parseColor("#D32F2F");
        if ("COMPLETED".equalsIgnoreCase(m.status)) color = Color.parseColor("#455A64");
        h.tvStatus.setTextColor(color);

        // ảnh sân (nếu có tên drawable)
        if (m.courtImage != null && !m.courtImage.isEmpty()) {
            int resId = ctx.getResources().getIdentifier(m.courtImage, "drawable", ctx.getPackageName());
            if (resId != 0) h.iv.setImageResource(resId);
        }
    }

    @Override public int getItemCount(){ return data.size(); }
}


