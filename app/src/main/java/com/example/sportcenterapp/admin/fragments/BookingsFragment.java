package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class BookingsFragment extends Fragment {

    private RecyclerView rv;
    private ChipGroup chips;

    private final List<BookingRow> all = new ArrayList<>();
    private final List<BookingRow> shown = new ArrayList<>();
    private BookingAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_bookings, container, false);
        rv = v.findViewById(R.id.rvBookings);
        chips = v.findViewById(R.id.chipGroupBookings);

        seedDemo();             // TODO: thay bằng dữ liệu thật từ SQLite
        shown.addAll(all);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(shown);
        rv.setAdapter(adapter);

        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String status = "TODAY";
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                status = ((Chip) group.findViewById(id)).getTag().toString();
            }
            filter(status);
        });

        ((Chip) v.findViewById(R.id.chipToday)).setChecked(true);
        return v;
    }

    private void filter(String status) {
        shown.clear();
        if ("TODAY".equals(status)) {
            for (BookingRow b : all) if ("Hôm nay".equals(b.day)) shown.add(b);
        } else if ("UPCOMING".equals(status)) {
            for (BookingRow b : all) if ("Sắp tới".equals(b.day)) shown.add(b);
        } else if ("PAST".equals(status)) {
            for (BookingRow b : all) if ("Đã qua".equals(b.day)) shown.add(b);
        } else if ("PENDING".equals(status)) {
            for (BookingRow b : all) if ("Chờ duyệt".equals(b.status)) shown.add(b);
        }
        adapter.notifyDataSetChanged();
    }

    private void seedDemo() {
        all.clear();
        all.add(new BookingRow("Sân 7 người",  "08:00–09:00", "Hôm nay",  "Chờ duyệt"));
        all.add(new BookingRow("Cầu lông 01",  "09:00–10:00", "Hôm nay",  "Đã duyệt"));
        all.add(new BookingRow("Sân BC 01",    "18:00–19:00", "Sắp tới",  "Chờ duyệt"));
        all.add(new BookingRow("Sân 7 người",  "08:00–09:00", "Đã qua",   "Hoàn tất"));
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.VH> {
        private final List<BookingRow> data;
        BookingAdapter(List<BookingRow> d) { this.data = d; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_admin_row, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            BookingRow b = data.get(pos);
            h.title.setText(b.court + " • " + b.time);
            h.subtitle.setText(b.day);
            h.status.setText(b.status);
            h.status.setBackgroundResource(bgFor(b.status));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle, status;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvTitle);
                subtitle = itemView.findViewById(R.id.tvSubtitle);
                status = itemView.findViewById(R.id.tvStatus);
            }
        }

        private static int bgFor(String s) {
            if ("Chờ duyệt".equals(s)) return R.drawable.bg_status_pending;
            if ("Hoàn tất".equals(s))  return R.drawable.bg_status_done;
            return R.drawable.bg_status_paid;
        }
    }

    private static class BookingRow {
        String court, time, day, status;
        BookingRow(String court, String time, String day, String status) {
            this.court = court; this.time = time; this.day = day; this.status = status;
        }
    }
}
