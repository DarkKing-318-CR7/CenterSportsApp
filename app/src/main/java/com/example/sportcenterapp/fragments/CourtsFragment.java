// CourtsFragment.java (đã sửa để cập nhật trạng thái sân vào SQLite)
package com.example.sportcenterapp.fragments;

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

    private RecyclerView recyclerView;
    private CourtAdapter adapter;
    private List<Court> courtList;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courts, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCourts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        courtList = dbHelper.getAllCourts();

        adapter = new CourtAdapter(getContext(), courtList, court -> {
            if (court.getStatus().equals("available")) {
                court.setStatus("booked");
                dbHelper.updateCourtStatus(court.getId(), "booked");
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Đặt sân thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sân đã được đặt", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
        return view;
    }
}