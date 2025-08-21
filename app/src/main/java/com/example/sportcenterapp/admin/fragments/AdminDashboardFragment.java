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
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rv;
    private ChipGroup chipFilters;
    private final List<Row> data = new ArrayList<>();
    private Adapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        rv = v.findViewById(R.id.rvList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter(data);
        rv.setAdapter(adapter);

        chipFilters = v.findViewById(R.id.chipFilters);
        chipFilters.setOnCheckedStateChangeListener((group, ids) -> {
            String tag = "ALL";
            if (!ids.isEmpty()) {
                View chip = group.findViewById(ids.get(0));
                if (chip != null && chip.getTag() != null) tag = chip.getTag().toString();
            }
            loadByTag(tag);
        });

        // Mặc định chọn "Tất cả"
        loadByTag("ALL");
    }

    private void loadByTag(String tag) {
        data.clear();
        switch (tag) {
            case "ALL": {
                data.add(Row.header("ĐƠN HÀNG"));
                data.addAll(queryOrders(null));        // ALL
                data.add(Row.header("ĐẶT SÂN"));
                data.addAll(queryBookings(null));      // ALL
                break;
            }
            case "PENDING_ALL": {
                data.add(Row.header("CHƯA DUYỆT • ĐƠN HÀNG"));
                data.addAll(queryOrders("pending"));
                data.add(Row.header("CHƯA DUYỆT • ĐẶT SÂN"));
                data.addAll(queryBookings("PENDING"));
                break;
            }
            case "APPROVED_ALL": {
                data.add(Row.header("ĐÃ DUYỆT • ĐƠN HÀNG"));
                data.addAll(queryOrders("approved_or_paid"));
                data.add(Row.header("ĐÃ DUYỆT • ĐẶT SÂN"));
                data.addAll(queryBookings("APPROVED"));
                break;
            }
            case "ORDERS_ONLY": {
                data.add(Row.header("ĐƠN HÀNG"));
                data.addAll(queryOrders(null));
                break;
            }
            case "BOOKINGS_ONLY": {
                data.add(Row.header("ĐẶT SÂN"));
                data.addAll(queryBookings(null));
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /* ================== ORDERS ================== */

    private boolean colExists(SQLiteDatabase r, String table, String col) {
        try (Cursor c = r.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                if (col.equalsIgnoreCase(name)) return true;
            }
        }
        return false;
    }

    /** statusKey: null=ALL, "pending"=chờ duyệt, "approved_or_paid"=đã duyệt (gom approved/paid/fulfilled) */
    private List<Row> queryOrders(@Nullable String statusKey) {
        ArrayList<Row> list = new ArrayList<>();
        SQLiteDatabase r = db.getReadableDatabase();
        boolean hasStatus  = colExists(r, "orders", "status");
        boolean hasCreated = colExists(r, "orders", "created_at");

        String dExpr = hasCreated ? "strftime('%Y-%m-%d', o.created_at)" : "date('now')";
        String where = "";
        if (hasStatus) {
            if ("pending".equals(statusKey)) {
                where = "WHERE o.status='pending' ";
            } else if ("approved_or_paid".equals(statusKey)) {
                where = "WHERE o.status IN ('approved','paid','fulfilled') ";
            }
        }

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
            case "pending":   return "PENDING";
            case "approved":
            case "paid":
            case "fulfilled": return "APPROVED";
            case "canceled":  return "CANCELLED";
        }
        return "PENDING";
    }

    /* ================== BOOKINGS ================== */

    /** statusKey: null=ALL, "PENDING", "APPROVED"(=CONFIRMED/DONE/COMPLETED) */
    private List<Row> queryBookings(@Nullable String statusKey) {
        ArrayList<Row> list = new ArrayList<>();
        SQLiteDatabase r = db.getReadableDatabase();

        String where = "";
        if ("PENDING".equals(statusKey)) {
            where = "WHERE b.status='PENDING' ";
        } else if ("APPROVED".equals(statusKey)) {
            where = "WHERE b.status IN ('CONFIRMED','DONE','COMPLETED') ";
        }

        String sql = "SELECT b.id, IFNULL(u.full_name,u.username), " +
                "IFNULL(b.total_price,0), b.status, c.name, b.date, b.start_time, b.end_time " +
                "FROM Bookings b JOIN Users u ON u.id=b.user_id " +
                "LEFT JOIN Courts c ON c.id=b.court_id " +
                where +
                "ORDER BY b.date DESC, b.id DESC";

        try (Cursor c = r.rawQuery(sql, null)) {
            String lastDay = null;
            while (c.moveToNext()) {
                String day = c.getString(5);
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
                        (start != null ? start : "") +
                        (end != null ? "–" + end : "") +
                        " • Dự kiến: " + fmt(total);
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
            case "COMPLETED":  return "APPROVED";
            case "CANCELLED":  return "CANCELLED";
        }
        return "PENDING";
    }

    /* ============ Utils & Adapter ============ */

    private static String fmt(double v) {
        return NumberFormat.getNumberInstance(new Locale("vi","VN"))
                .format(Math.round(v)) + " đ";
    }

    private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM   = 1;
        private final List<Row> data;
        Adapter(List<Row> d) { this.data = d; }

        @Override public int getItemViewType(int position) {
            return data.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            if (vt == TYPE_HEADER) {
                View v = LayoutInflater.from(p.getContext())
                        .inflate(R.layout.item_section_header, p, false);
                return new HeaderVH(v);
            } else {
                View v = LayoutInflater.from(p.getContext())
                        .inflate(R.layout.item_admin_row, p, false);
                return new ItemVH(v);
            }
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            Row r = data.get(pos);
            if (h instanceof HeaderVH) {
                ((HeaderVH) h).title.setText(r.headerText);
            } else {
                ItemVH ivh = (ItemVH) h;
                ivh.title.setText(r.code + " • " + r.customer);
                ivh.subtitle.setText(r.extra != null ? r.extra : ("Tổng: " + fmt(r.total)));
                ivh.status.setText(labelVi(r.status));
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
            ItemVH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tvTitle);
                subtitle = v.findViewById(R.id.tvSubtitle);
                status = v.findViewById(R.id.tvStatus);
            }
        }

        private static String labelVi(String ui){
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
                case "APPROVED":  return R.drawable.bg_status_done;
                case "CANCELLED": return R.drawable.bg_status_cancel;
                default:          return R.drawable.bg_status_pending;
            }
        }
    }

    private static class Row {
        final boolean isHeader;
        final String headerText; // với header
        final String code, customer, status, extra; // với item
        final double total;

        private Row(boolean isHeader, String headerText, String code, String customer,
                    double total, String status, String extra) {
            this.isHeader = isHeader; this.headerText = headerText;
            this.code = code; this.customer = customer; this.total = total;
            this.status = status; this.extra = extra;
        }
        static Row header(String text) { return new Row(true, text, null, null, 0, null, null); }
        static Row order(String code, String name, double total, String status) {
            return new Row(false, null, code, name, total, status, null);
        }
        static Row booking(String code, String name, double total, String status, String extra) {
            return new Row(false, null, code, name, total, status, extra);
        }
    }
}
