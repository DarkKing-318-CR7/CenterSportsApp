package com.example.sportcenterapp.player.fragments;

import com.example.sportcenterapp.models.Booking;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

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
    private View emptyView;
    private DatabaseHelper db;
    private SessionManager session;

    @Override public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle b) {
        View v = inf.inflate(R.layout.fragment_booking_history, parent, false);
        rv = v.findViewById(R.id.rvBookings);
        emptyView = v.findViewById(R.id.emptyView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        loadData();
        return v;
    }

    private void loadData() {
        // DÙNG FQN để trùng đúng models.Booking
        java.util.List<com.example.sportcenterapp.models.Booking> data =
                db.getBookingsByUser(session.getUserId());
        rv.setAdapter(new BookingHistoryAdapter(data));
        emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
