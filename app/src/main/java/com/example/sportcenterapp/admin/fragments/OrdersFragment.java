package com.example.sportcenterapp.admin.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.OrderItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.*;

public class OrdersFragment extends Fragment {

    public static final String ACTION_ORDERS_CHANGED = "com.example.sportcenterapp.ACTION_ORDERS_CHANGED";

    private RecyclerView rv;
    private ChipGroup chips;
    private final List<Row> shown = new ArrayList<>();
    private Adapter adapter;
    private DatabaseHelper db;
    private String uiStatus = "ALL"; // ALL|PENDING|APPROVED|CANCELLED

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        rv = v.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter(shown, new Adapter.ActionListener() {
            @Override public void onApprove(long orderId) { setStatus(orderId, "approved"); }
            @Override public void onReject(long orderId)  { setStatus(orderId, "cancelled"); }
            @Override public void onOpenDetail(long orderId) { openDetailSheet(orderId); }
        });
        rv.setAdapter(adapter);

        chips = v.findViewById(R.id.chipGroupOrders);
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            uiStatus = "ALL";
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                if (c != null && c.getTag() != null) uiStatus = c.getTag().toString();
            }
            load();
        });

        // nếu layout cũ còn chip "Hoàn tất" -> đổi text trong xml thành "Đã duyệt" và tag="APPROVED"
        Chip chipAll = v.findViewById(R.id.chipAll);
        if (chipAll != null) chipAll.setChecked(true);

        load();
    }

    private void setStatus(long orderId, String newStatusDb) {
        db.updateOrderStatus(orderId, newStatusDb);
        load();
    }

    private void load() {
        shown.clear();
        shown.addAll(queryOrders(uiStatus));
        adapter.notifyDataSetChanged();
    }

    private boolean hasCol(SQLiteDatabase r, String table, String col) {
        try (Cursor c = r.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) {
                if (col.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) return true;
            }
        }
        return false;
    }

    private @Nullable String uiToDb(String ui) {
        if (ui == null || "ALL".equalsIgnoreCase(ui)) return null;
        switch (ui) {
            case "PENDING":   return "pending";
            case "APPROVED":  return "approved";
            case "CANCELLED": return "cancelled";
        }
        return null;
    }

    /** Lấy đơn + gộp nhanh danh sách món: “Tên xSL, ...” */
    private List<Row> queryOrders(String ui) {
        ArrayList<Row> list = new ArrayList<>();
        SQLiteDatabase r = db.getReadableDatabase();
        boolean hasStatus = hasCol(r, "orders", "status");
        boolean hasCreated = hasCol(r, "orders", "created_at");

        String dExpr = hasCreated ? "strftime('%Y-%m-%d', o.created_at)" : "date('now')";
        String where = "";
        String[] args = null;
        String s = uiToDb(ui);
        if (s != null && hasStatus) { where = "WHERE o.status=? "; args = new String[]{s}; }

        String sql =
                "SELECT o.id, IFNULL(u.full_name,u.username) AS customer, o.total " +
                        (hasStatus ? ", o.status " : ", 'pending' AS status ") +
                        ", " + dExpr + " AS d, " +
                        "IFNULL((SELECT group_concat(name || ' x' || quantity, ', ') " +
                        "        FROM order_items WHERE order_id=o.id),'') AS summary " +
                        "FROM orders o JOIN Users u ON u.id=o.user_id " +
                        where +
                        "ORDER BY d DESC, o.id DESC";

        try (Cursor c = r.rawQuery(sql, args)) {
            String lastDay = null;
            while (c.moveToNext()) {
                String day = c.getString(4);
                if (lastDay == null || !lastDay.equals(day)) {
                    list.add(Row.header(day));
                    lastDay = day;
                }
                long id       = c.getLong(0);
                String name   = c.getString(1);
                double total  = c.getDouble(2);
                String dbStat = c.getString(3);
                String uiStat = mapDbToUi(dbStat);
                String summary= c.getString(5);

                String sub = summary.isEmpty()
                        ? ("Tổng: " + fmt(total))
                        : (summary + " • Tổng: " + fmt(total));

                list.add(Row.item(id, "#OD-" + id, name, total, uiStat, sub));
            }
        }
        return list;
    }

    private String mapDbToUi(String s) {
        if (s == null) return "PENDING";
        switch (s.toLowerCase(Locale.ROOT)) {
            case "pending":   return "PENDING";
            case "approved":  return "APPROVED";
            case "cancelled": return "CANCELLED";
            case "fulfilled": return "APPROVED"; // nếu cũ còn "fulfilled" coi như đã duyệt
        }
        return "PENDING";
    }

    private static String fmt(double v) {
        return NumberFormat.getNumberInstance(new Locale("vi","VN"))
                .format(Math.round(v)) + " đ";
    }

    /* ===== Bottom sheet chi tiết đơn ===== */
    private void openDetailSheet(long orderId) {
        BottomSheetDialog dlg = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.sheet_order_detail, null, false);
        dlg.setContentView(v);

        TextView tvTitle = v.findViewById(R.id.tvOrderCode);
        TextView tvTotal = v.findViewById(R.id.tvOrderTotal);
        RecyclerView rvItems = v.findViewById(R.id.rvOrderItems);
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Lấy items an toàn
        List<OrderItem> items;
        try {
            items = db.getOrderItems((int) orderId);
            if (items == null) items = new ArrayList<>();
        } catch (Exception e) {
            items = new ArrayList<>();
        }
        rvItems.setAdapter(new ItemsAdapter(items));

        tvTitle.setText("#OD-" + orderId);

        double total = 0;
        for (OrderItem it : items) total += it.price * it.quantity;
        tvTotal.setText("Tổng: " + NumberFormat.getNumberInstance(new Locale("vi","VN"))
                .format(Math.round(total)) + " đ");

        dlg.show();
    }


    /* ===== Adapter ===== */

    private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        interface ActionListener {
            void onApprove(long orderId);
            void onReject(long orderId);
            void onOpenDetail(long orderId);
        }

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM   = 1;

        private final List<Row> data;
        private final ActionListener listener;

        Adapter(List<Row> d, ActionListener l) { this.data = d; this.listener = l; }

        @Override public int getItemViewType(int position) {
            return data.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            if (vt == TYPE_HEADER) {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_section_header, p, false);
                return new HeaderVH(v);
            } else {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_order_action, p, false);
                return new ItemVH(v);
            }
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            Row r = data.get(pos);
            if (h instanceof HeaderVH) {
                ((HeaderVH) h).title.setText("Ngày " + r.headerDay);
            } else {
                ItemVH v = (ItemVH) h;
                v.title.setText(r.code + " • " + r.customer);
                v.subtitle.setText(r.extra);
                v.status.setText(labelFor(r.status));
                v.status.setBackgroundResource(bgFor(r.status));

                // card click -> xem chi tiết
                v.itemView.setOnClickListener(view -> listener.onOpenDetail(r.id));

                // chỉ hiện nút khi đang chờ duyệt
                int vis = "PENDING".equals(r.status) ? View.VISIBLE : View.GONE;
                v.btnApprove.setVisibility(vis);
                v.btnReject.setVisibility(vis);

                v.btnApprove.setOnClickListener(view -> listener.onApprove(r.id));
                v.btnReject.setOnClickListener(view -> listener.onReject(r.id));
            }
        }

        @Override public int getItemCount() { return data.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            TextView title;
            HeaderVH(@NonNull View v) { super(v); title = v.findViewById(R.id.tvSectionTitle); }
        }
        static class ItemVH extends RecyclerView.ViewHolder {
            TextView title, subtitle, status, btnApprove, btnReject;
            ItemVH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tvTitle);
                subtitle = v.findViewById(R.id.tvSubtitle);
                status = v.findViewById(R.id.tvStatus);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject  = v.findViewById(R.id.btnReject);
            }
        }

        private static String labelFor(String s){
            if (s==null) return "Chờ duyệt";
            switch (s){
                case "PENDING":   return "Chờ duyệt";
                case "APPROVED":  return "Đã duyệt";
                case "CANCELLED": return "Đã huỷ";
            }
            return "Chờ duyệt";
        }
        private static int bgFor(String s){
            if (s==null) return R.drawable.bg_status_pending;
            switch (s){
                case "PENDING":   return R.drawable.bg_status_pending;
                case "APPROVED":  return R.drawable.bg_status_done;   // dùng nền xanh có sẵn
                case "CANCELLED": return R.drawable.bg_status_cancel;
            }
            return R.drawable.bg_status_pending;
        }
    }

    /* Đối tượng hàng hiển thị */
    private static class Row {
        final boolean isHeader;
        final String headerDay;
        final long id;
        final String code, customer, status, extra;
        final double total;

        private Row(boolean isHeader, String headerDay, long id, String code, String customer,
                    double total, String status, String extra) {
            this.isHeader = isHeader; this.headerDay = headerDay;
            this.id = id; this.code = code; this.customer = customer;
            this.total = total; this.status = status; this.extra = extra;
        }
        static Row header(String day) { return new Row(true, day, 0, null, null, 0, null, null); }
        static Row item(long id, String code, String name, double total, String status, String extra) {
            return new Row(false, null, id, code, name, total, status, extra);
        }
    }

    /* Adapter items cho bottom sheet */
    private static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.VH> {
        private final java.util.List<OrderItem> data;
        ItemsAdapter(java.util.List<OrderItem> d) { this.data = d; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(@NonNull View v){ super(v); tv = v.findViewById(android.R.id.text1); }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            TextView t = new TextView(p.getContext());
            t.setId(android.R.id.text1);
            t.setPadding(24,16,24,16);
            return new VH(t);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            OrderItem it = data.get(pos);
            h.tv.setText("• " + it.name + " x" + it.quantity + " — " +
                    NumberFormat.getNumberInstance(new Locale("vi","VN"))
                            .format(Math.round(it.price * it.quantity)) + " đ");
        }
        @Override public int getItemCount(){ return data.size(); }
    }
}
