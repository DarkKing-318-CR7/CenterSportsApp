package com.example.sportcenterapp.admin.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.AdminActivity;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BookingsFragment extends Fragment {

    private ChipGroup chipGroup;
    private RecyclerView rv;
    private SwipeRefreshLayout swipe;

    private final List<Row> all = new ArrayList<>();
    private final List<Row> shown = new ArrayList<>();
    private Adapter adapter;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_bookings, container, false);

        chipGroup = v.findViewById(R.id.chipGroupBookings);
        rv = v.findViewById(R.id.rvBookings);
        swipe = v.findViewById(R.id.swipeRefresh);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter(shown, this::onApprove, this::onReject);
        rv.setAdapter(adapter);

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            String tag = "TODAY";
            if (!ids.isEmpty()) tag = String.valueOf(((Chip) group.findViewById(ids.get(0))).getTag());
            applyFilter(tag);
        });
        ((Chip) v.findViewById(R.id.chipToday)).setChecked(true);

        swipe.setOnRefreshListener(this::reload);

        reload();
        return v;
    }

    private void reload() {
        loadFromDb();
        // giữ filter hiện tại
        int checkedId = chipGroup.getCheckedChipId();
        String tag = "TODAY";
        if (checkedId != View.NO_ID) tag = String.valueOf(((Chip) chipGroup.findViewById(checkedId)).getTag());
        applyFilter(tag);
        swipe.setRefreshing(false);
    }

    private void loadFromDb() {
        all.clear();
        Context ctx = getContext();
        if (ctx == null) return;
        DatabaseHelper db = new DatabaseHelper(ctx);

        // Cố gắng JOIN Courts nếu có court_id; nếu không có, đọc từ cột tên ảnh trong Bookings
        String sql =
                "SELECT b.id AS bid, b.date, b.start_time, b.end_time, b.status, b.total_price, " +
                        "       c.name AS court_name, c.image AS court_image " +
                        "FROM Bookings b LEFT JOIN Courts c ON b.court_id = c.id " +
                        "ORDER BY b.date DESC, b.start_time DESC, b.id DESC";

        try (Cursor cur = db.getReadableDatabase().rawQuery(sql, null)) {
            while (cur.moveToNext()) {
                Row r = new Row();
                r.id = getLong(cur, "bid", "id");
                r.date = getString(cur, "date");
                r.start = getString(cur, "start_time", "startTime");
                r.end   = getString(cur, "end_time", "endTime");
                r.status= getString(cur, "status");
                r.price = getInt(cur, "total_price", "price");
                r.court = getString(cur, "court_name", "courtName");
                r.image = getString(cur, "court_image", "image");
                if (r.court == null) r.court = "—";
                all.add(r);
            }
        } catch (Exception e) {
            // Fallback: đọc trực tiếp từ Bookings nếu schema khác
            try (Cursor cur = db.getReadableDatabase().rawQuery(
                    "SELECT id, date, start_time, end_time, status, total_price, courtName, image FROM Bookings " +
                            "ORDER BY date DESC, start_time DESC, id DESC", null)) {

                while (cur.moveToNext()) {
                    Row r = new Row();
                    r.id = cur.getLong(0);
                    r.date = cur.getString(1);
                    r.start = cur.getString(2);
                    r.end = cur.getString(3);
                    r.status = cur.getString(4);
                    r.price = cur.getInt(5);
                    r.court = cur.getString(6);
                    r.image = cur.getString(7);
                    all.add(r);
                }
            }
        }

        updatePendingBadge();
    }

    private void updatePendingBadge() {
        int count = 0;
        for (Row r : all) if ("PENDING".equalsIgnoreCase(r.status)) count++;
        if (getActivity() == null) return;
        BottomNavigationView bottom = getActivity().findViewById(R.id.bottomAdmin);
        if (bottom != null) {
            var badge = bottom.getOrCreateBadge(R.id.menu_bookings);
            badge.setVisible(count > 0);
            badge.setNumber(count);
        }
    }

    private void applyFilter(String tag) {
        shown.clear();
        switch (tag) {
            case "PENDING":
                for (Row r : all) if ("PENDING".equalsIgnoreCase(r.status)) shown.add(r);
                break;
            case "UPCOMING":
                for (Row r : all) if (dayCat(r.date) > 0) shown.add(r); // tương lai
                break;
            case "PAST":
                for (Row r : all) if (dayCat(r.date) < 0) shown.add(r); // đã qua
                break;
            default: // TODAY
                for (Row r : all) if (dayCat(r.date) == 0) shown.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    // -1: quá khứ, 0: hôm nay, +1: tương lai
    private int dayCat(String yyyyMMdd) {
        if (yyyyMMdd == null) return 0;
        try {
            Date d = df.parse(yyyyMMdd);
            Calendar c1 = Calendar.getInstance(); zero(c1);
            Calendar c2 = Calendar.getInstance(); c2.setTime(d); zero(c2);
            if (c2.before(c1)) return -1;
            if (c2.after(c1)) return 1;
            return 0;
        } catch (ParseException e) { return 0; }
    }
    private void zero(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
    }

    private void onApprove(Row r) {
        Context ctx = getContext(); if (ctx == null) return;
        if (!"PENDING".equalsIgnoreCase(r.status)) {
            Toast.makeText(ctx, "Chỉ duyệt đơn đang chờ.", Toast.LENGTH_SHORT).show(); return;
        }
        new MaterialAlertDialogBuilder(ctx)
                .setTitle("Duyệt đặt sân")
                .setMessage(r.court + " • " + r.start + "–" + r.end + "\n" + r.date)
                .setPositiveButton("Duyệt", (d,w) -> {
                    new DatabaseHelper(ctx).updateBookingStatus(r.id, "CONFIRMED");
                    Toast.makeText(ctx, "Đã duyệt", Toast.LENGTH_SHORT).show();
                    reload();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onReject(Row r) {
        Context ctx = getContext(); if (ctx == null) return;
        if (!"PENDING".equalsIgnoreCase(r.status)) {
            Toast.makeText(ctx, "Chỉ từ chối đơn đang chờ.", Toast.LENGTH_SHORT).show(); return;
        }
        new MaterialAlertDialogBuilder(ctx)
                .setTitle("Từ chối đặt sân")
                .setMessage(r.court + " • " + r.start + "–" + r.end + "\n" + r.date)
                .setPositiveButton("Từ chối", (d,w) -> {
                    new DatabaseHelper(ctx).updateBookingStatus(r.id, "CANCELLED");
                    Toast.makeText(ctx, "Đã từ chối", Toast.LENGTH_SHORT).show();
                    reload();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ----------------- Model nhẹ cho UI -----------------
    private static class Row {
        long id;
        String court, image, date, start, end, status;
        int price;
    }

    // ----------------- RecyclerView Adapter -----------------
    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        interface Action { void call(Row r); }
        private final List<Row> data;
        private final Action approve, reject;
        Adapter(List<Row> data, Action approve, Action reject) {
            this.data = data; this.approve = approve; this.reject = reject;
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_admin_booking, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Row r = data.get(pos);
            h.tvTitle.setText(r.court != null ? r.court : "—");
            h.tvTime.setText(r.date + " • " + r.start + "–" + r.end);
            h.tvPrice.setText(nf().format(r.price) + " đ");
            // ảnh theo tên drawable nếu có
            Context ctx = h.itemView.getContext();
            int resId = 0;
            if (r.image != null && !r.image.isEmpty())
                resId = ctx.getResources().getIdentifier(r.image, "drawable", ctx.getPackageName());
            if (resId == 0) resId = R.drawable.placeholder_court;
            h.img.setImageResource(resId);

            // status chip
            String st = r.status == null ? "PENDING" : r.status.toUpperCase(Locale.ROOT);
            h.tvStatus.setText(mapStatusText(st));
            h.tvStatus.setBackgroundResource(bgFor(st));

            h.btnApprove.setOnClickListener(v -> approve.call(r));
            h.btnReject.setOnClickListener(v -> reject.call(r));

            // long-press cũng mở action
            h.itemView.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(v.getContext())
                        .setTitle("Cập nhật trạng thái")
                        .setItems(new String[]{"Duyệt (CONFIRMED)", "Từ chối (CANCELLED)"}, (d, w) -> {
                            if (w == 0) h.btnApprove.performClick(); else h.btnReject.performClick();
                        }).show();
                return true;
            });
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView tvTitle, tvTime, tvPrice, tvStatus;
            View btnApprove, btnReject;
            VH(@NonNull View v) {
                super(v);
                img = v.findViewById(R.id.imgCourt);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvTime = v.findViewById(R.id.tvTime);
                tvPrice = v.findViewById(R.id.tvPrice);
                tvStatus = v.findViewById(R.id.tvStatus);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject = v.findViewById(R.id.btnReject);
            }
        }

        private static NumberFormat nf() { return NumberFormat.getNumberInstance(new Locale("vi","VN")); }
        private static int bgFor(String st) {
            switch (st) {
                case "CONFIRMED":  return R.drawable.bg_status_paid;
                case "CANCELLED":  return R.drawable.bg_status_cancel;
                case "COMPLETED":  return R.drawable.bg_status_done;
                default:           return R.drawable.bg_status_pending; // PENDING
            }
        }
        private static String mapStatusText(String st) {
            switch (st) {
                case "CONFIRMED":  return "Đã duyệt";
                case "CANCELLED":  return "Từ chối";
                case "COMPLETED":  return "Hoàn tất";
                default:           return "Chờ duyệt";
            }
        }
    }

    // helpers để lấy cột theo nhiều tên (tránh lệch schema)
    private static String getString(Cursor c, String... names) {
        for (String n : names) {
            int i = c.getColumnIndex(n);
            if (i >= 0) return c.getString(i);
        }
        return null;
    }
    private static int getInt(Cursor c, String... names) {
        for (String n : names) {
            int i = c.getColumnIndex(n);
            if (i >= 0) return c.getInt(i);
        }
        return 0;
    }
    // helpers để lấy cột theo nhiều tên (an toàn với schema khác nhau)
    private static long getLong(Cursor c, String... names) {
        for (String n : names) {
            int i = c.getColumnIndex(n);
            if (i >= 0) return c.getLong(i);
        }
        return 0L;
    }

}
