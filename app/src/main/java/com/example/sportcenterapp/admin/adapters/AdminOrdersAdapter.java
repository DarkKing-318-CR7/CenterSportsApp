package com.example.sportcenterapp.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.ApiService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.VH> {

    public interface OnAction {
        void onClick(ApiService.OrderAdminDTO o);
        void onApprove(ApiService.OrderAdminDTO o);
        void onCancel(ApiService.OrderAdminDTO o);
    }

    private final List<ApiService.OrderAdminDTO> ds = new ArrayList<>();
    private final OnAction cb;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));

    public AdminOrdersAdapter(OnAction cb) {
        this.cb = cb;
    }

    public void setData(List<ApiService.OrderAdminDTO> list){
        ds.clear();
        if (list != null) ds.addAll(list);
        notifyDataSetChanged();
    }

    private static String coalesce(String... xs){
        for (String s : xs) if (s != null && !s.isEmpty()) return s;
        return null;
    }
    private static Integer coalesceInt(Integer... xs){
        for (Integer i : xs) if (i != null) return i;
        return null;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUser, tvStatus, tvTotal, tvTime;
        Button btnApprove, btnCancel;
        VH(@NonNull View v){
            super(v);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvUser    = v.findViewById(R.id.tvUser);
            tvStatus  = v.findViewById(R.id.tvStatus);
            tvTotal   = v.findViewById(R.id.tvTotal);
            tvTime    = v.findViewById(R.id.tvTime);
            btnApprove= v.findViewById(R.id.btnApprove);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_order, p, false);
        return new VH(v);
    }

    private static double toDouble(Object v){
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e){ return 0; }
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ApiService.OrderAdminDTO o = ds.get(pos);

        h.tvOrderId.setText("#" + o.id);

        // Ưu tiên tên; nếu không có, fallback hiển thị User #id; nếu vẫn null => "Người dùng"
        String name = coalesce(o.user_name);
        Integer uid = coalesceInt(o.user_id);
        String displayUser = name != null ? name : (uid != null ? ("User #" + uid) : "Người dùng");
        h.tvUser.setText(displayUser);

        h.tvStatus.setText(o.status != null ? o.status : "PENDING");
        h.tvTime.setText(o.created_at != null ? o.created_at : "");

        double totalVal = toDouble(o.total);
        long total = Math.round(totalVal);
        h.tvTotal.setText(nf.format(total) + "đ");

        h.itemView.setOnClickListener(v -> { if (cb != null) cb.onClick(o); });
        h.btnApprove.setOnClickListener(v -> { if (cb != null) cb.onApprove(o); });
        h.btnCancel.setOnClickListener(v -> { if (cb != null) cb.onCancel(o); });

        boolean pending = "PENDING".equalsIgnoreCase(o.status);
        h.btnApprove.setVisibility(pending ? View.VISIBLE : View.GONE);
        h.btnCancel.setVisibility(pending ? View.VISIBLE : View.GONE);
    }

    @Override public int getItemCount() { return ds.size(); }
}
