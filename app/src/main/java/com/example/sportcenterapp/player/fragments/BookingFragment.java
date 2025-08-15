package com.example.sportcenterapp.player.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.TimeSlotAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingFragment extends Fragment {

    // UI
    private ImageView imgCourt;
    private TextView tvCourtName, tvCourtMeta, tvCourtPrice;
    private Spinner spCourt;
    private TextView tvDate;
    private RecyclerView rvSlots;
    private Button btnBook;

    // Data
    private final Calendar cal = Calendar.getInstance();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<Court> courts = new ArrayList<>();
    private Court selectedCourt;
    private String startSel, endSel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_booking, container, false);

        // Bind views
        imgCourt     = v.findViewById(R.id.imgCourt);
        tvCourtName  = v.findViewById(R.id.tvCourtName);
        tvCourtMeta  = v.findViewById(R.id.tvCourtMeta);
        tvCourtPrice = v.findViewById(R.id.tvCourtPrice);
        spCourt      = v.findViewById(R.id.spCourt);
        tvDate       = v.findViewById(R.id.tvDate);
        rvSlots      = v.findViewById(R.id.rvSlots);
        btnBook      = v.findViewById(R.id.btnBook);

        rvSlots.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseHelper db = new DatabaseHelper(getContext());
        courts = db.getCourts();

        // Spinner sân
        ArrayAdapter<Court> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courts);
        spCourt.setAdapter(ad);
        spCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourt = courts.get(position);
                bindCourtHeader(selectedCourt);
                loadSlots(); // reload slot theo sân + ngày
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Chọn ngày
        tvDate.setText(df.format(cal.getTime()));
        tvDate.setOnClickListener(vw -> new DatePickerDialog(requireContext(),
                (picker, y, m, d) -> {
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                    tvDate.setText(df.format(cal.getTime()));
                    loadSlots();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show());

        // Đặt sân
        btnBook.setOnClickListener(vw -> {
            if (selectedCourt == null || startSel == null) return;
            String date = df.format(cal.getTime());
            DatabaseHelper db2 = new DatabaseHelper(getContext());

            if (db2.hasConflict(selectedCourt.id, date, startSel, endSel)) {
                Toast.makeText(getContext(), "Khung giờ đã có người đặt!", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = new SessionManager(requireContext()).getUserId();
            double total = selectedCourt.price; // 1 giờ
            long id = db2.createBooking(userId, selectedCourt.id, date, startSel, endSel, total);
            if (id > 0) {
                Toast.makeText(getContext(),
                        "Đã gửi yêu cầu đặt sân. Vui lòng chờ quản trị duyệt.",
                        Toast.LENGTH_LONG).show();
                startSel = endSel = null;
                btnBook.setEnabled(false);
                loadSlots();
            } else {
                Toast.makeText(getContext(), "Lỗi đặt sân", Toast.LENGTH_SHORT).show();
            }

        });

        // Mặc định chọn sân đầu
        if (!courts.isEmpty()) {
            selectedCourt = courts.get(0);
            bindCourtHeader(selectedCourt);
            loadSlots();
        } else {
            // Không có sân
            tvCourtName.setText("Chưa có sân");
            btnBook.setEnabled(false);
        }

        return v;
    }

    /** Cập nhật phần header: ảnh + tên + meta + giá */
    private void bindCourtHeader(@NonNull Court c) {
        tvCourtName.setText(c.name);

        String meta = c.sport + " • " + (c.indoor==1 ? "Trong nhà" : "Ngoài trời");
        if (c.surface != null && !c.surface.isEmpty()) meta += " • " + c.surface;
        tvCourtMeta.setText(meta);

        tvCourtPrice.setText(String.format(Locale.getDefault(), "%,.0fđ/giờ", c.price));

        int resId = getResources().getIdentifier(
                (c.image == null || c.image.isEmpty()) ? "placeholder_court" : c.image,
                "drawable",
                requireContext().getPackageName()
        );
        imgCourt.setImageResource(resId == 0 ? R.drawable.placeholder_court : resId);
    }

    /** Tạo danh sách slot 60' từ 06:00–22:00 và gắn adapter chọn 1 slot */
    private void loadSlots() {
        startSel = endSel = null;
        btnBook.setEnabled(false);

        List<String[]> slots = new ArrayList<>();
        for (int h = 6; h < 22; h++) {
            String s = String.format(Locale.getDefault(), "%02d:00", h);
            String e = String.format(Locale.getDefault(), "%02d:00", h + 1);
            slots.add(new String[]{s, e});
        }

        TimeSlotAdapter adapter = new TimeSlotAdapter(slots, (s, e) -> {
            startSel = s;
            endSel = e;
            btnBook.setEnabled(true);
        });
        rvSlots.setAdapter(adapter);
    }
}
