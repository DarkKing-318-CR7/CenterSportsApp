package com.example.sportcenterapp.player.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.BookingHistoryAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryFragment extends Fragment {

    private RecyclerView rv;
    private DatabaseHelper db;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_history, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        rv = v.findViewById(R.id.rvBookingHistory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new BookingHistoryAdapter(getData()));
    }

    private List<Object[]> getData() {
        List<Object[]> out = new ArrayList<>();
        String sql = "SELECT b.date, b.start_time, b.end_time, c.name AS court_name, b.total_price " +
                "FROM Bookings b JOIN Courts c ON b.court_id=c.id " +
                "WHERE b.user_id=? ORDER BY b.date DESC, b.start_time DESC";
        try (Cursor c = db.getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(session.getUserId())})) {
            while (c.moveToNext()) {
                out.add(new Object[]{
                        c.getString(0), // date
                        c.getString(1), // start
                        c.getString(2), // end
                        c.getString(3), // court_name
                        c.getDouble(4)  // total_price
                });
            }
        }
        return out;
    }
}
