package com.example.sportcenterapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sportcenterapp.R;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.VH> {

    public interface OnPick { void onPicked(String start, String end); }

    private final List<String[]> data = new ArrayList<>();
    private int selected = -1;
    private final OnPick callback;

    public TimeSlotAdapter(List<String[]> slots, OnPick cb) {
        if (slots != null) data.addAll(slots);
        this.callback = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View v) { super(v); tv = v.findViewById(R.id.tvSlot); }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_slot, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        String s = data.get(pos)[0], e = data.get(pos)[1];
        h.tv.setText(s + " - " + e);
        h.tv.setBackgroundColor(pos == selected ? Color.parseColor("#C5E1A5") : Color.parseColor("#EDE7F6"));
        h.itemView.setOnClickListener(v -> {
            int old = selected; selected = h.getAdapterPosition();
            notifyItemChanged(old); notifyItemChanged(selected);
            if (callback != null) callback.onPicked(s, e);
        });
    }

    @Override public int getItemCount() { return data.size(); }
}
