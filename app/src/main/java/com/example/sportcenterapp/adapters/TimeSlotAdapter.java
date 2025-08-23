package com.example.sportcenterapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.VH> {

    public interface OnPick { void onPicked(String start, String end); }

    private final List<String[]> ds;
    private final OnPick cb;

    public TimeSlotAdapter(List<String[]> ds, OnPick cb) {
        this.ds = ds;
        this.cb = cb;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String[] row = ds.get(position);
        String start = row.length > 0 ? row[0] : "";
        String end   = row.length > 1 ? row[1] : "";
        boolean taken = row.length > 2 && "1".equals(row[2]); // "1" = đã đặt

        h.tv.setText(start + " - " + end);

        if (taken) {
            h.tv.setEnabled(false);
            h.itemView.setEnabled(false);
            h.tv.setTextColor(Color.GRAY);
            h.tv.setBackgroundColor(Color.parseColor("#EEEEEE"));
            h.itemView.setOnClickListener(null);
        } else {
            h.tv.setEnabled(true);
            h.itemView.setEnabled(true);
            h.tv.setTextColor(Color.BLACK);
            h.tv.setBackgroundResource(R.drawable.bg_slot_available);
            h.itemView.setOnClickListener(v -> cb.onPicked(start, end));
        }
    }

    @Override
    public int getItemCount() {
        return ds != null ? ds.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        public VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvSlot);
        }
    }
}
