package com.example.sportcenterapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;

import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        db = new DatabaseHelper(requireContext());

        TextView tvTotal = v.findViewById(R.id.tvTotalRevenue);
        TextView tvToday = v.findViewById(R.id.tvTodayRevenue);
        TextView tvApproved = v.findViewById(R.id.tvApprovedOrders);
        TextView tvPending = v.findViewById(R.id.tvPendingOrders);
        LinearLayout topContainer = v.findViewById(R.id.containerTopProducts);

        tvTotal.setText(String.format(Locale.getDefault(),
                "Tổng doanh thu: %.0f", db.getTotalRevenue()));
        tvToday.setText(String.format(Locale.getDefault(),
                "Hôm nay: %.0f", db.getTodayRevenue()));
        tvApproved.setText("Đơn đã duyệt: " + db.getApprovedCourtOrderCount());
        tvPending.setText("Đơn chờ duyệt: " + db.getPendingCourtOrderCount());

        topContainer.removeAllViews();
        List<String> tops = db.getTopProducts();
        for (String s : tops) {
            TextView t = new TextView(requireContext());
            t.setText("• " + s);
            topContainer.addView(t);
        }

        return v;
    }
}
