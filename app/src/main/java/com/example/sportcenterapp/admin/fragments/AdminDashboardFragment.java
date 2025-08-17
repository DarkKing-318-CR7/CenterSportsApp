package com.example.sportcenterapp.admin.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.sportcenterapp.database.DatabaseHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tổng quan: 2 nút “Đơn hàng / Đặt sân”; có filter mini & group theo ngày */
public class AdminDashboardFragment extends Fragment {

    private static final int MODE_ORDERS   = 0;
    private static final int MODE_BOOKINGS = 1;

    private int mode = MODE_ORDERS;

    // CHỈ DÙNG 4 TRẠNG THÁI: ALL | PENDING | APPROVED | CANCELLED
    private String ordersUiStatus   = "ALL";
    private String bookingsUiStatus = "ALL";

    private DatabaseHelper db;
    private RecyclerView rv;
    private ChipGroup chipOrderFilters, chipBookingFilters;

    private final List<Row> data = new ArrayList<>();
    private Adapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        rv = v.findViewById(R.id.rvList);   // <-- CHÍNH XÁC LÀ rvList
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter(data);
        rv.setAdapter(adapter);


        chipOrderFilters   = v.findViewById(R.id.chipOrderFilters);
        chipBookingFilters = v.findViewById(R.id.chipBookingFilters);

        // Toggle “Đơn hàng / Đặt sân”
        MaterialButtonToggleGroup tg = v.findViewById(R.id.toggleGroup);
        tg.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnToggleOrders) {
                mode = MODE_ORDERS;
                chipOrderFilters.setVisibility(View.VISIBLE);
                chipBookingFilters.setVisibility(View.GONE);
            } else if (checkedId == R.id.btnToggleBookings) {
                mode = MODE_BOOKINGS;
                chipOrderFilters.setVisibility(View.GONE);
                chipBookingFilters.setVisibility(View.VISIBLE);
            }
            load();
        });
        tg.check(R.id.btnToggleOrders);

        // Mini filter: Đơn hàng
        chipOrderFilters.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) {
                Chip c = group.findViewById(ids.get(0));
                ordersUiStatus = (c != null && c.getTag()!=null) ? c.getTag().toString() : "ALL";
            } else ordersUiStatus = "ALL";
            if (mode == MODE_ORDERS) load();
        });
        // chọn mặc định
        Chip chipDashAll = v.findViewById(R.id.chipDashAll);
        if (chipDashAll != null) chipDashAll.setChecked(true);

        // Mini filter: Đặt sân
        chipBookingFilters.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) {
                Chip c = group.findViewById(ids.get(0));
                bookingsUiStatus = (c != null && c.getTag()!=null) ? c.getTag().toString() : "ALL";
            } else bookingsUiStatus = "ALL";
            if (mode == MODE_BOOKINGS) load();
        });
        Chip chipBkAll = v.findViewById(R.id.chipBkAll);
        if (chipBkAll != null) chipBkAll.setChecked(true);

        load();
    }

    private void load() {
        data.clear();
        if (mode == MODE_ORDERS) data.addAll(queryOrders(ordersUiStatus));
        else                     data.addAll(queryBookings(bookingsUiStatus));
        adapter.notifyDataSetChanged();
    }

    /* ====================== ORDERS ====================== */

    private boolean colExists(SQLiteDatabase r, String table, String col) {
        try (Cursor c = r.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) {
                if (col.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) return true;
            }
        }
        return false;
    }

    /** where cho đơn hàng với 4 trạng thái chuẩn hoá */
    private String makeOrdersWhere(boolean hasStatus, String uiTag) {
        if (!hasStatus || uiTag == null || "ALL".equals(uiTag)) return "";
        switch (uiTag) {
            case "PENDING":
                return "WHERE o.status='pending' ";
            case "APPROVED":
                // tương thích dữ liệu cũ: 'paid'/'fulfilled'
                return "WHERE o.status IN ('approved','paid','fulfilled') ";
            case "CANCELLED":
                return "WHERE o.status='canceled' ";
            default:
                return "";
        }
    }

    private List<Row> queryOrders(String uiTag) {
        ArrayList<Row> list = new ArrayList<>();
        SQLiteDatabase r = db.getReadableDatabase();
        boolean hasStatus  = colExists(r, "orders", "status");
        boolean hasCreated = colExists(r, "orders", "created_at");

        String dExpr = hasCreated ? "strftime('%Y-%m-%d', o.created_at)" : "date('now')";
        String where = makeOrdersWhere(hasStatus, uiTag);

        // 0:id,1:customer,2:total,3:status,4:day
        String sql = "SELECT o.id, IFNULL(u.full_name,u.username), o.total" +
                (hasStatus ? ", o.status" : ", NULL AS status") +
                ", " + dExpr + " AS d " +
                "FROM orders o JOIN Users u ON u.id=o.user_id " +
                where +
                "ORDER BY d DESC, o.id DESC";

        try (Cursor c = r.rawQuery(sql, null)) {
            String lastDay = null;
            while (c.moveToNext()) {
                String day = c.getString(4);
                if (lastDay == null || !lastDay.equals(day)) {
                    list.add(Row.header(day));
                    lastDay = day;
                }
                long id      = c.getLong(0);
                String name  = c.getString(1);
                double total = c.getDouble(2);
                String uiSt  = mapOrderStatus(hasStatus ? c.getString(3) : "pending");
                list.add(Row.order("#OD-" + id, name, total, uiSt));
            }
        }
        return list;
    }

    private String mapOrderStatus(String s) {
        if (s == null) return "PENDING";
        switch (s.toLowerCase(Locale.ROOT)) {
            case "pending":    return "PENDING";
            case "approved":   return "APPROVED";
            case "paid":
            case "fulfilled":  return "APPROVED";   // gom về ĐÃ DUYỆT
            case "canceled":   return "CANCELLED";
        }
        return "PENDING";
    }

    /* ===================== BOOKINGS ===================== */

    /** where cho đặt sân với 4 trạng thái chuẩn hoá */
    private String makeBookingsWhere(String uiTag) {
        if (uiTag == null || "ALL".equals(uiTag)) return "";
        switch (uiTag) {
            case "PENDING":
                return "WHERE b.status='PENDING' ";
            case "APPROVED":
                // CONFIRMED/DONE/COMPLETED xem như đã duyệt
                return "WHERE b.status IN ('CONFIRMED','DONE','COMPLETED') ";
            case "CANCELLED":
                return "WHERE b.status='CANCELLED' ";
            default:
                return "";
        }
    }

    private List<Row> queryBookings(String uiTag) {
        ArrayList<Row> list = new ArrayList<>();
        SQLiteDatabase r = db.getReadableDatabase();

        String where = makeBookingsWhere(uiTag);

        // 0:id,1:customer,2:total,3:status,4:court,5:date,6:start,7:end
        String sql = "SELECT b.id, IFNULL(u.full_name,u.username), " +
                "IFNULL(b.total_price,0), b.status, c.name, b.date, b.start_time, b.end_time " +
                "FROM Bookings b JOIN Users u ON u.id=b.user_id " +
                "LEFT JOIN Courts c ON c.id=b.court_id " +
                where +
                "ORDER BY b.date DESC, b.id DESC";

        try (Cursor c = r.rawQuery(sql, null)) {
            String lastDay = null;
            while (c.moveToNext()) {
                String day = c.getString(5); // yyyy-MM-dd
                if (lastDay == null || !lastDay.equals(day)) {
                    list.add(Row.header(day));
                    lastDay = day;
                }
                long id      = c.getLong(0);
                String name  = c.getString(1);
                double total = c.getDouble(2);
                String uiSt  = mapBookingStatus(c.getString(3));
                String court = c.isNull(4) ? null : c.getString(4);
                String start = c.getString(6);
                String end   = c.getString(7);

                String extra = (court != null ? ("Sân: " + court + " • ") : "") +
                        start + "–" + end + " • Dự kiến: " + fmt(total);
                list.add(Row.booking("#BK-" + id, name, total, uiSt, extra));
            }
        }
        return list;
    }

    private String mapBookingStatus(String s) {
        if (s == null) return "PENDING";
        switch (s.toUpperCase(Locale.ROOT)) {
            case "PENDING":    return "PENDING";
            case "CONFIRMED":
            case "DONE":
            case "COMPLETED":  return "APPROVED";   // gom về ĐÃ DUYỆT
            case "CANCELLED":  return "CANCELLED";
        }
        return "PENDING";
    }

    /* ===== Helpers ===== */
    private static String fmt(double v) {
        return NumberFormat.getNumberInstance(new Locale("vi","VN"))
                .format(Math.round(v)) + " đ";
    }

    /* =================== Adapter section =================== */

    private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM   = 1;

        private final List<Row> data;
        Adapter(List<Row> d) { this.data = d; }

        @Override public int getItemViewType(int position) {
            return data.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            if (vt == TYPE_HEADER) {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_section_header, p, false);
                return new HeaderVH(v);
            } else {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_row, p, false);
                return new ItemVH(v);
            }
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            Row r = data.get(pos);
            if (h instanceof HeaderVH) {
                ((HeaderVH) h).title.setText("Ngày " + r.headerDay);
            } else {
                ItemVH ivh = (ItemVH) h;
                ivh.title.setText(r.code + " • " + r.customer);
                ivh.subtitle.setText(r.extra != null ? r.extra : ("Tổng: " + fmt(r.total)));
                ivh.status.setText(viLabel(r.status));
                ivh.status.setBackgroundResource(bgFor(r.status));
            }
        }

        @Override public int getItemCount() { return data.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            TextView title;
            HeaderVH(@NonNull View v) { super(v); title = v.findViewById(R.id.tvSectionTitle); }
        }
        static class ItemVH extends RecyclerView.ViewHolder {
            TextView title, subtitle, status;
            ItemVH(@NonNull View v) { super(v);
                title = v.findViewById(R.id.tvTitle);
                subtitle = v.findViewById(R.id.tvSubtitle);
                status = v.findViewById(R.id.tvStatus);
            }
        }

        private static String viLabel(String ui){
            if (ui == null) return "Chờ duyệt";
            switch (ui){
                case "PENDING":   return "Chờ duyệt";
                case "APPROVED":  return "Đã duyệt";
                case "CANCELLED": return "Đã huỷ";
                default:          return "Chờ duyệt";
            }
        }

        private static int bgFor(String ui){
            if (ui==null) return R.drawable.bg_status_pending;
            switch (ui){
                case "PENDING":   return R.drawable.bg_status_pending;
                case "APPROVED":  return R.drawable.bg_status_done;    // dùng nền xanh lá
                case "CANCELLED": return R.drawable.bg_status_cancel;
                default:          return R.drawable.bg_status_pending;
            }
        }
    }

    /* ================ Model cho Adapter ================ */
    private static class Row {
        final boolean isHeader;
        final String headerDay;    // khi isHeader = true
        final String code, customer, status, extra;
        final double total;

        private Row(boolean isHeader, String headerDay, String code, String customer,
                    double total, String status, String extra) {
            this.isHeader = isHeader; this.headerDay = headerDay;
            this.code = code; this.customer = customer; this.total = total;
            this.status = status; this.extra = extra;
        }
        static Row header(String day) { return new Row(true, day, null, null, 0, null, null); }
        static Row order(String code, String name, double total, String status) {
            return new Row(false, null, code, name, total, status, null);
        }
        static Row booking(String code, String name, double total, String status, String extra) {
            return new Row(false, null, code, name, total, status, extra);
        }
    }
}
