package com.example.sportcenterapp.player.fragments;

import android.view.*;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.net.*;
import com.example.sportcenterapp.utils.SessionManager;
import java.util.*;
import retrofit2.*;

public class BookingHistoryFragment extends Fragment {
    private ProgressBar progress; private TextView empty;
    private RecyclerView rv; private HistoryAdapter adapter;
    private final List<ApiService.Booking> data = new ArrayList<>();
    private ApiService api; private SessionManager session;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_booking_history, c, false);
        progress = v.findViewById(R.id.progressHistory);
        empty    = v.findViewById(R.id.emptyView);
        rv       = v.findViewById(R.id.rvBookingHistory);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(data, bookingId -> cancelBooking(bookingId));
        rv.setAdapter(adapter);


        api = ApiClient.get().create(ApiService.class);
        session = new SessionManager(requireContext());
        loadHistory();
        return v;
    }
    private void cancelBooking(int bookingId){
        Integer uid = new SessionManager(requireContext()).getUserId();
        if (uid==null || uid<=0) { Toast.makeText(getContext(),"Vui lòng đăng nhập lại",Toast.LENGTH_SHORT).show(); return; }
        setLoading(true);
        ApiService api = ApiClient.get().create(ApiService.class);
        api.cancelBookingByUser(new ApiService.CancelReq(uid, bookingId))
                .enqueue(new retrofit2.Callback<ApiService.SimpleResp>() {
                    @Override public void onResponse(retrofit2.Call<ApiService.SimpleResp> c, retrofit2.Response<ApiService.SimpleResp> r) {
                        setLoading(false);
                        if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                            Toast.makeText(getContext(),"Đã huỷ booking",Toast.LENGTH_SHORT).show();
                            loadHistory(); // refresh list
                        } else {
                            Toast.makeText(getContext(),"Không thể huỷ",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<ApiService.SimpleResp> c, Throwable t) {
                        setLoading(false);
                        Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setLoading(boolean b){ if(progress!=null) progress.setVisibility(b?View.VISIBLE:View.GONE); }

    private void loadHistory() {
        Integer uid = session.getUserId();
        if (uid==null || uid<=0) { Toast.makeText(getContext(),"Vui lòng đăng nhập lại",Toast.LENGTH_SHORT).show(); return; }
        setLoading(true);
        api.getBookingsByUser(uid).enqueue(new Callback<List<ApiService.Booking>>() {
            @Override public void onResponse(Call<List<ApiService.Booking>> c, Response<List<ApiService.Booking>> r) {
                setLoading(false);
                if (!r.isSuccessful() || r.body()==null) { Toast.makeText(getContext(),"Không tải được lịch sử",Toast.LENGTH_SHORT).show(); return; }
                data.clear(); data.addAll(r.body()); adapter.notifyDataSetChanged();
                empty.setVisibility(data.isEmpty()? View.VISIBLE: View.GONE);
            }
            @Override public void onFailure(Call<List<ApiService.Booking>> c, Throwable t) {
                setLoading(false); Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---- Thay cả class adapter cũ bằng đoạn này ----
    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        interface Action { void onCancel(int bookingId); }
        private final List<ApiService.Booking> ds;
        private final Action action;
        HistoryAdapter(List<ApiService.Booking> ds, Action action){ this.ds = ds; this.action = action; }

        static class VH extends RecyclerView.ViewHolder {
            ImageView ivCourt;
            TextView tvCourt, tvDate, tvTime, tvPrice, tvStatus;
            Button btnCancel;
            VH(View v){
                super(v);
                ivCourt   = v.findViewById(R.id.ivCourt);
                tvCourt   = v.findViewById(R.id.tvCourt);
                tvDate    = v.findViewById(R.id.tvDate);
                tvTime    = v.findViewById(R.id.tvTime);
                tvPrice   = v.findViewById(R.id.tvPrice);
                tvStatus  = v.findViewById(R.id.tvStatus);
                btnCancel = v.findViewById(R.id.btnCancelBooking);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_booking_history, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            ApiService.Booking b = ds.get(pos);

            h.tvCourt.setText(b.court_name != null ? b.court_name : ("Sân #" + b.court_id));
            h.tvDate.setText(b.date != null ? b.date : "");
            if (b.image != null && !b.image.isEmpty()) {
                Glide.with(h.ivCourt.getContext())
                        .load(b.image)
                        .placeholder(R.drawable.placeholder_court)
                        .error(R.drawable.placeholder_court)
                        .into(h.ivCourt);
            } else {
                h.ivCourt.setImageResource(R.drawable.placeholder_court);
            }

            h.tvTime.setText(
                    (b.start_time != null ? b.start_time : "") +
                            " - " +
                            (b.end_time   != null ? b.end_time   : "")
            );
            h.tvPrice.setText(String.valueOf((long) b.total_price) + " đ");
            h.tvStatus.setText(b.status != null ? b.status : "PENDING");

            // màu nền status (nếu đã tạo 3 drawable)
            if ("CONFIRMED".equals(b.status))      h.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            else if ("CANCELLED".equals(b.status)) h.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            else                                   h.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);

            // Hiện nút Huỷ khi PENDING và còn trước giờ bắt đầu
            boolean showCancel = false;
            if ("PENDING".equals(b.status)) {
                try {
                    String st = (b.start_time != null && b.start_time.length() >= 8) ? b.start_time : (b.start_time + ":00");
                    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    java.util.Date start = f.parse(b.date + " " + st);
                    showCancel = (start != null && new java.util.Date().before(start));
                } catch (Exception ignored) { }
            }
            h.btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            h.btnCancel.setOnClickListener(v -> action.onCancel(b.id));
        }

        @Override public int getItemCount(){ return ds.size(); }
    }

}
