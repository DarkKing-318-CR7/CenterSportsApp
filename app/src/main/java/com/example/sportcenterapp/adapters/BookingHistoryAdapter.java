package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;

import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.VH> {
    private final List<Object[]> data;
    public BookingHistoryAdapter(List<Object[]> data) { this.data = data; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_booking_history, p, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Object[] row = data.get(pos);
        String date = (String) row[0], st = (String) row[1], en = (String) row[2];
        String court = (String) row[3];
        double price = (double) row[4];
        h.tvTitle.setText(court);
        h.tvTime.setText(date + " • " + st + "-" + en);
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", price));
    }
    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvPrice;
        VH(@NonNull View item) {
            super(item);
            tvTitle = item.findViewById(R.id.tvTitle);
            tvTime = item.findViewById(R.id.tvTime);
            tvPrice = item.findViewById(R.id.tvPrice);
        }
    }
}

