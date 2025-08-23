package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsFragment extends Fragment {

    private ChipGroup chipGroup;     // R.id.chipGroupBookings
    private SwipeRefreshLayout swipe;// R.id.swipeRefresh
    private RecyclerView rv;         // R.id.rvBookings

    private final List<ApiService.BookingAdmin> data = new ArrayList<>();
    private AdminAdapter adapter;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_admin_bookings, c, false);

        chipGroup = v.findViewById(R.id.chipGroupBookings);
        swipe     = v.findViewById(R.id.swipeRefresh);
        rv        = v.findViewById(R.id.rvBookings);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminAdapter(data, this::updateStatus);
        rv.setAdapter(adapter);

        api = ApiClient.get().create(ApiService.class);

        // Nếu chip XML chưa set tag, set tạm theo text hiển thị
        initChipTagsIfMissing();

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> refresh());
        swipe.setOnRefreshListener(this::refresh);

        // load lần đầu
        refresh();
        return v;
    }

    private void initChipTagsIfMissing() {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;           // <-- cast kiểu cũ
                if (chip.getTag() == null) {
                    String txt = chip.getText() != null ? chip.getText().toString().toLowerCase() : "";
                    // gán tag chuẩn cho API
                    if (txt.contains("hôm nay") || txt.contains("today")) {
                        chip.setTag("today");
                    } else if (txt.contains("sắp tới") || txt.contains("upcoming")) {
                        chip.setTag("upcoming");
                    } else if (txt.contains("đã qua") || txt.contains("past")) {
                        chip.setTag("past");
                    } else if (txt.contains("chờ duyệt") || txt.contains("pending")) {
                        chip.setTag("pending");
                    }
                }
            }
        }
    }


    private void refresh() {
        swipe.setRefreshing(true);
        String[] filter = mapChipToFilters(); // [status, when]
        String status = filter[0];
        String when   = filter[1];

        api.getBookingsAdmin(status, when).enqueue(new Callback<List<ApiService.BookingAdmin>>() {
            @Override public void onResponse(Call<List<ApiService.BookingAdmin>> c, Response<List<ApiService.BookingAdmin>> r) {
                swipe.setRefreshing(false);
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(getContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                    return;
                }
                data.clear();
                data.addAll(r.body());
                adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<ApiService.BookingAdmin>> c, Throwable t) {
                swipe.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Trả về [status, when]
    private String[] mapChipToFilters() {
        int id = chipGroup.getCheckedChipId();
        if (id == View.NO_ID) return new String[]{"", "today"}; // mặc định: hôm nay
        Chip chip = chipGroup.findViewById(id);
        String tag = chip != null && chip.getTag() != null ? chip.getTag().toString() : "";

        switch (tag) {
            case "pending":  return new String[]{"PENDING", ""};     // lọc theo trạng thái chờ duyệt
            case "today":    return new String[]{"", "today"};
            case "upcoming": return new String[]{"", "upcoming"};
            case "past":     return new String[]{"", "past"};
            default:         return new String[]{"", ""};            // tất cả
        }
    }

    private void updateStatus(int bookingId, String newStatus) {
        swipe.setRefreshing(true);
        api.updateBookingStatus(new ApiService.UpdateStatusReq(bookingId, newStatus))
                .enqueue(new Callback<ApiService.SimpleResp>() {
                    @Override public void onResponse(Call<ApiService.SimpleResp> c, Response<ApiService.SimpleResp> r) {
                        swipe.setRefreshing(false);
                        if (r.isSuccessful() && r.body() != null && r.body().ok) {
                            Toast.makeText(getContext(), "Đã cập nhật: " + newStatus, Toast.LENGTH_SHORT).show();
                            refresh();
                        } else {
                            Toast.makeText(getContext(), "Không cập nhật được", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<ApiService.SimpleResp> c, Throwable t) {
                        swipe.setRefreshing(false);
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Adapter hiển thị item_admin_booking.xml ---
    static class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {
        interface Action { void run(int bookingId, String newStatus); }
        private final List<ApiService.BookingAdmin> ds;
        private final Action action;
        AdminAdapter(List<ApiService.BookingAdmin> ds, Action action){ this.ds = ds; this.action = action; }

        static class VH extends RecyclerView.ViewHolder {
            ImageView imgCourt; TextView tvTitle, tvTime, tvPrice, tvStatus;
            Button btnApprove, btnReject;
            VH(View v){
                super(v);
                imgCourt   = v.findViewById(R.id.imgCourt);
                tvTitle    = v.findViewById(R.id.tvTitle);
                tvTime     = v.findViewById(R.id.tvTime);
                tvPrice    = v.findViewById(R.id.tvPrice);
                tvStatus   = v.findViewById(R.id.tvStatus);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject  = v.findViewById(R.id.btnReject);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_booking, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int i) {
            ApiService.BookingAdmin b = ds.get(i);

            // Ảnh sân
            if (b.image != null && !b.image.isEmpty()) {
                Glide.with(h.imgCourt.getContext())
                        .load(b.image)
                        .placeholder(R.drawable.placeholder_court)
                        .error(R.drawable.placeholder_court)
                        .into(h.imgCourt);
            } else {
                h.imgCourt.setImageResource(R.drawable.placeholder_court);
            }

            // Tiêu đề + thời gian + giá
            h.tvTitle.setText((b.court_name != null ? b.court_name : ("Sân #" + b.court_id)) +
                    " • " + (b.user_name != null ? b.user_name : ("User #" + b.user_id)));
            h.tvTime.setText((b.date != null ? b.date : "") + "  " +
                    (b.start_time != null ? b.start_time : "") + " - " +
                    (b.end_time != null ? b.end_time : ""));
            h.tvPrice.setText(String.valueOf((long)b.total_price) + " đ");

            // Status badge
            h.tvStatus.setText(b.status != null ? b.status : "PENDING");
            if ("CONFIRMED".equals(b.status))      h.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            else if ("CANCELLED".equals(b.status)) h.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            else                                   h.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);

            // Nút duyệt/huỷ chỉ bật khi đang PENDING
            boolean pending = "PENDING".equals(b.status);
            h.btnApprove.setEnabled(pending);
            h.btnReject.setEnabled(pending);
            h.btnApprove.setOnClickListener(v -> action.run(b.id, "CONFIRMED"));
            h.btnReject.setOnClickListener(v -> action.run(b.id, "CANCELLED"));
        }

        @Override public int getItemCount(){ return ds.size(); }
    }
}
