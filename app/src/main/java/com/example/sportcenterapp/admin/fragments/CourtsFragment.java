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
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Court;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Admin > Courts: liệt kê sân + chuẩn bị hook thêm/sửa/xóa
 */
public class CourtsFragment extends Fragment {

    private RecyclerView rv;
    private final List<Court> data = new ArrayList<>();
    private DatabaseHelper db;
    private CourtsSimpleAdapter adapter; // nếu bạn có CourtsAdapter riêng, đổi type & new ở dưới

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_admin_courts, parent, false);

        rv = v.findViewById(R.id.rvCourts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = new DatabaseHelper(requireContext());
        adapter = new CourtsSimpleAdapter(data);
        rv.setAdapter(adapter);

        loadData();
        return v;
    }

    private void loadData() {
        data.clear();
        data.addAll(db.getAllCourts()); // ✅ dùng API mới, trả về models.Court
        adapter.notifyDataSetChanged();
    }

    // ========= Simple adapter gọn (nếu bạn đã có adapter riêng thì bỏ class này) =========
    private static class CourtsSimpleAdapter
            extends RecyclerView.Adapter<CourtsSimpleAdapter.VH> {

        private final List<Court> items;

        CourtsSimpleAdapter(List<Court> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_court, parent, false); // <-- dùng đúng layout
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Court c = items.get(pos);

            // Phòng ngừa null nếu layout chưa đồng bộ
            if (h.tvName != null) h.tvName.setText(c.name);

            String meta = c.sport + " • " + (c.indoor == 1 ? "Trong nhà" : "Ngoài trời");
            if (c.surface != null && !c.surface.isEmpty()) meta += " • " + c.surface;
            if (h.tvMeta != null) h.tvMeta.setText(meta);

            if (h.tvPrice != null) {
                h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ/giờ", c.price));
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName, tvMeta, tvPrice;
            VH(@NonNull View v) {
                super(v);
                tvName  = v.findViewById(R.id.tvName);
                tvMeta  = v.findViewById(R.id.tvMeta);
                tvPrice = v.findViewById(R.id.tvPrice);
            }
        }
    }

}
