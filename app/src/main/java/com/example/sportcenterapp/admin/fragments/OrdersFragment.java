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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {

    private RecyclerView rv;
    private ChipGroup chips;

    private final List<OrderRow> all = new ArrayList<>();
    private final List<OrderRow> shown = new ArrayList<>();
    private OrdersAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_orders, container, false);
        rv = v.findViewById(R.id.rvOrders);
        chips = v.findViewById(R.id.chipGroupOrders);

        seedDemo();             // TODO: thay bằng dữ liệu thật từ SQLite
        shown.addAll(all);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(shown);
        rv.setAdapter(adapter);

        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String status = "ALL";
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                status = ((Chip) group.findViewById(id)).getTag().toString();
            }
            filter(status);
        });

        // chọn mặc định "Tất cả"
        ((Chip) v.findViewById(R.id.chipAll)).setChecked(true);
        return v;
    }

    private void filter(String status) {
        shown.clear();
        if ("ALL".equals(status)) {
            shown.addAll(all);
        } else {
            for (OrderRow o : all) if (o.status.equals(status)) shown.add(o);
        }
        adapter.notifyDataSetChanged();
    }

    private void seedDemo() {
        all.clear();
        all.add(new OrderRow("#OD-1001", "Nguyễn A", 250000, "PENDING"));
        all.add(new OrderRow("#OD-1002", "Trần B",   120000, "PAID"));
        all.add(new OrderRow("#OD-1003", "Lê C",     150000, "CANCELLED"));
        all.add(new OrderRow("#OD-1004", "Phạm D",   300000, "DONE"));
        all.add(new OrderRow("#OD-1005", "Võ E",     180000, "PENDING"));
    }

    /** ----- Adapter đơn giản để demo ----- */
    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {
        private final List<OrderRow> data;
        OrdersAdapter(List<OrderRow> d) { this.data = d; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_admin_row, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            OrderRow o = data.get(pos);
            h.title.setText(o.code + " • " + o.customer);
            h.subtitle.setText("Tổng: " + fmt(o.total));
            h.status.setText(mapStatus(o.status));
            h.status.setBackgroundResource(bgFor(o.status));
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

        private static String fmt(int v) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(v) + " đ";
        }
        private static String mapStatus(String s) {
            switch (s) {
                case "PENDING": return "Chờ duyệt";
                case "PAID": return "Đã thanh toán";
                case "DONE": return "Hoàn tất";
                case "CANCELLED": return "Hủy";
            }
            return s;
        }
        private static int bgFor(String s) {
            switch (s) {
                case "PENDING":   return R.drawable.bg_status_pending;
                case "PAID":      return R.drawable.bg_status_paid;
                case "DONE":      return R.drawable.bg_status_done;
                case "CANCELLED": return R.drawable.bg_status_cancel;
            }
            return R.drawable.bg_status_pending;
        }
    }

    /** Row dữ liệu demo */
    private static class OrderRow {
        String code, customer, status; int total;
        OrderRow(String code, String customer, int total, String status) {
            this.code = code; this.customer = customer; this.total = total; this.status = status;
        }
    }
}
