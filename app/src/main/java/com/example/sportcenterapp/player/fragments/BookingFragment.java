package com.example.sportcenterapp.player.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.TimeSlotAdapter;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {

    private ImageView imgCourt;
    private TextView tvCourtName, tvCourtMeta, tvCourtPrice, tvDate;
    private Spinner spCourt;
    private RecyclerView rvSlots;            // id: rvCourtsBooking
    private ProgressBar progress;
    private Button btnBook;

    private final List<Court> courts = new ArrayList<>();
    private ArrayAdapter<String> courtSpinnerAdapter;
    private final List<String[]> slotData = new ArrayList<>();

    private TimeSlotAdapter timeSlotAdapter;

    private Court selectedCourt = null;
    private String selectedDate;            // yyyy-MM-dd
    private String selectedStart = null;    // HH:mm:ss
    private String selectedEnd = null;      // HH:mm:ss

    private ApiService api;
    private SessionManager session;

    private final SimpleDateFormat dfYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dfShow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_booking, container, false);

        imgCourt     = v.findViewById(R.id.imgCourt);
        tvCourtName  = v.findViewById(R.id.tvCourtName);
        tvCourtMeta  = v.findViewById(R.id.tvCourtMeta);
        tvCourtPrice = v.findViewById(R.id.tvCourtPrice);
        spCourt      = v.findViewById(R.id.spCourt);
        tvDate       = v.findViewById(R.id.tvDate);
        progress     = v.findViewById(R.id.progressBooking);
        rvSlots      = v.findViewById(R.id.rvCourtsBooking);
        btnBook      = v.findViewById(R.id.btnBook);

        session = new SessionManager(requireContext());
        api = ApiClient.build().create(ApiService.class);

        // Ngày mặc định = hôm nay
        Calendar now = Calendar.getInstance();
        selectedDate = dfYMD.format(now.getTime());
        tvDate.setText(dfShow.format(now.getTime()));

        tvDate.setOnClickListener(v1 -> showDatePicker());

        // Spinner sân
        courtSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spCourt.setAdapter(courtSpinnerAdapter);
        spCourt.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < courts.size()) {
                    selectedCourt = courts.get(position);
                    renderCourtInfo(selectedCourt);
                    buildSlotsForCourt();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // RecyclerView khung giờ
        rvSlots.setLayoutManager(new LinearLayoutManager(getContext()));
// 1) Adapter rỗng ban đầu
        timeSlotAdapter = new TimeSlotAdapter(slotData, (start, end) -> {
            selectedStart = start.length()==5 ? start + ":00" : start;
            selectedEnd   = end.length()==5 ? end + ":00" : end;
            btnBook.setEnabled(true);
        });
        rvSlots.setAdapter(timeSlotAdapter);



        // Nút Đặt sân
        btnBook.setEnabled(false);
        btnBook.setOnClickListener(v12 -> {
            if (selectedCourt == null || TextUtils.isEmpty(selectedDate)
                    || TextUtils.isEmpty(selectedStart) || TextUtils.isEmpty(selectedEnd)) {
                Toast.makeText(getContext(), "Chọn sân, ngày và khung giờ trước", Toast.LENGTH_SHORT).show();
                return;
            }
            double price = extractPriceFromText(tvCourtPrice.getText().toString()); // lấy từ TextView
            createBooking(selectedCourt.id, selectedDate, selectedStart, selectedEnd, price);
        });

        loadCourts();
        return v;
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        try {
            String t = tvDate.getText() != null ? tvDate.getText().toString() : null;
            if (t != null && !t.isEmpty()) c.setTime(dfShow.parse(t));
        } catch (Exception ignored) {}
        DatePickerDialog dp = new DatePickerDialog(requireContext(), (DatePicker view, int y, int m, int d) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(y, m, d, 0, 0, 0);
            selectedDate = dfYMD.format(picked.getTime());
            tvDate.setText(dfShow.format(picked.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void setLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
        btnBook.setEnabled(!show && selectedStart != null);
    }

    private void loadCourts() {
        setLoading(true);
        api.getCourts(null).enqueue(new Callback<List<Court>>() {
            @Override public void onResponse(@NonNull Call<List<Court>> call, @NonNull Response<List<Court>> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(getContext(), "Không tải được danh sách sân", Toast.LENGTH_SHORT).show();
                    return;
                }
                courts.clear();
                courts.addAll(resp.body());

                List<String> names = new ArrayList<>();
                for (Court c : courts) names.add(c.name);
                courtSpinnerAdapter.clear();
                courtSpinnerAdapter.addAll(names);
                courtSpinnerAdapter.notifyDataSetChanged();

                if (!courts.isEmpty()) {
                    spCourt.setSelection(0);
                    buildSlotsForCourt();}
                else {
                    selectedCourt = null;
                    slotData.clear();
                    timeSlotAdapter.notifyDataSetChanged();
                    btnBook.setEnabled(false);
                }
            }

            @Override public void onFailure(@NonNull Call<List<Court>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCourtInfo(Court c) {
        tvCourtName.setText(c.name != null ? c.name : "");
        String meta = "";
        if (c.sport != null)   meta += c.sport;
        if (c.surface != null && !c.surface.isEmpty()) meta += (meta.isEmpty() ? "" : " · ") + c.surface;
        if (c.indoor == 1)     meta += (meta.isEmpty() ? "" : " · ") + "Trong nhà";
        tvCourtMeta.setText(meta);

        // Hiển thị giá (nếu model bạn có getter riêng, thay ở đây)
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        double price = 0; // mặc định 0 nếu model không có trường
        try { // nếu bạn có c.getPricePerHour() hoặc c.price, có thể đổi ở đây
            // price = c.getPricePerHour();
        } catch (Exception ignored) {}
        tvCourtPrice.setText(nf.format(price) + " đ/giờ");

        if (c.image != null && !c.image.isEmpty()) {
            Glide.with(imgCourt.getContext())
                    .load(c.image)
                    .placeholder(R.drawable.placeholder_court)
                    .error(R.drawable.placeholder_court)
                    .into(imgCourt);
        } else {
            imgCourt.setImageResource(R.drawable.placeholder_court);
        }

    }

    private void buildSlotsForCourt() {
        selectedStart = selectedEnd = null;
        btnBook.setEnabled(false);

        if (selectedCourt == null || TextUtils.isEmpty(selectedDate)) return;

        setLoading(true);
        api.getTakenSlots(selectedCourt.id, selectedDate)
                .enqueue(new Callback<List<ApiService.TakenSlot>>() {
                    @Override public void onResponse(Call<List<ApiService.TakenSlot>> call,
                                                     Response<List<ApiService.TakenSlot>> resp) {
                        setLoading(false);

                        // 1) Chuẩn hoá tập slot đã đặt về định dạng "HH:mm-HH:mm"
                        java.util.HashSet<String> takenSet = new java.util.HashSet<>();
                        if (resp.isSuccessful() && resp.body()!=null) {
                            for (ApiService.TakenSlot t : resp.body()) {
                                String s = (t.start_time!=null && t.start_time.length()>=5) ? t.start_time.substring(0,5) : "";
                                String e = (t.end_time  !=null && t.end_time.length()  >=5) ? t.end_time.substring(0,5)   : "";
                                if (!s.isEmpty() && !e.isEmpty()) takenSet.add(s + "-" + e);
                            }
                        }

                        // 2) Dựng slot 07:00 → 21:00 và gắn cờ 1/0 theo takenSet
                        java.util.List<String[]> tmp = new java.util.ArrayList<>();
                        int startHour = 7, endHour = 21;
                        for (int h = startHour; h < endHour; h++) {
                            String s = String.format(java.util.Locale.getDefault(), "%02d:00", h);
                            String e = String.format(java.util.Locale.getDefault(), "%02d:00", h+1);
                            boolean isTaken = takenSet.contains(s + "-" + e);
                            tmp.add(new String[]{s, e, isTaken ? "1" : "0"});
                        }

                        slotData.clear();
                        slotData.addAll(tmp);

                        // 3) Cập nhật adapter
                        if (timeSlotAdapter == null) {
                            timeSlotAdapter = new TimeSlotAdapter(slotData, (start, end) -> {
                                selectedStart = start.length()==5 ? start + ":00" : start;
                                selectedEnd   = end.length()==5 ? end + ":00"   : end;
                                btnBook.setEnabled(true);
                            });
                            rvSlots.setAdapter(timeSlotAdapter);
                        } else {
                            timeSlotAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override public void onFailure(Call<List<ApiService.TakenSlot>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(getContext(),"Lỗi tải slot: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                });
    }



    private double extractPriceFromText(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try { return Double.parseDouble(digits); } catch (Exception e) { return 0; }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (selectedCourt != null && !TextUtils.isEmpty(selectedDate)) {
            buildSlotsForCourt();
        }
    }
    private void createBooking(int courtId, String date, String start, String end, double price) {
        Integer userId = null;
        try { userId = session.getUserId(); } catch (Exception ignored) {}
        if (userId == null || userId <= 0) {
            Toast.makeText(getContext(), "Không xác định được người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.BookingCreateReq req = new ApiService.BookingCreateReq(
                userId, courtId, date, start, end, price
        );

        setLoading(true);
        api.createBooking(req).enqueue(new Callback<ApiService.BookingCreateResp>() {
            @Override public void onResponse(@NonNull Call<ApiService.BookingCreateResp> call,
                                             @NonNull Response<ApiService.BookingCreateResp> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(getContext(), "Tạo booking thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiService.BookingCreateResp r = resp.body();
                if (r.ok) {
                    Toast.makeText(getContext(), "Đặt sân thành công! Mã #" + r.booking_id, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), r.error != null ? r.error : "Khung giờ đã được đặt", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<ApiService.BookingCreateResp> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }
}
