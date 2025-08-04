package com.example.sportcenterapp.fragments;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CourtAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Court;

import java.util.List;

public class CourtsFragment extends Fragment {

    RecyclerView recyclerView;
    CourtAdapter adapter;
    DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courts, container, false);

        recyclerView = view.findViewById(R.id.recyclerCourts);
        db = new DatabaseHelper(getContext());

        List<Court> courts = db.getAllCourts();
        adapter = new CourtAdapter(getContext(), courts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // XỬ LÝ ĐẶT SÂN – Đặt TRƯỚC return
        adapter.setOnBookClickListener(court -> {
            String[] timeOptions = {"7:00 - 8:00", "8:00 - 9:00", "9:00 - 10:00"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Chọn thời gian đặt cho " + court.getName())
                    .setItems(timeOptions, (dialog, which) -> {
                        String selectedTime = timeOptions[which];
                        boolean success = db.bookCourt(1, court.getId(), selectedTime);
                        if (success) {
                            Toast.makeText(getContext(), "Đặt sân thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Thất bại! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });

        return view; // TRẢ VIEW SAU CÙNG
    }
}