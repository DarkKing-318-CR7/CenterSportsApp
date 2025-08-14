package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CoachSimpleAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Coach;

import java.util.ArrayList;
import java.util.List;

public class CoachFragment extends Fragment {
    private RecyclerView rv;
    private Spinner spnSport;
    private DatabaseHelper db;
    private CoachSimpleAdapter adapter;

    @Override public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle b) {
        View v = inf.inflate(R.layout.fragment_coach_simple, parent, false);
        rv = v.findViewById(R.id.rvCoaches);
        spnSport = v.findViewById(R.id.spnSport);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());

        // Spinner dữ liệu
        List<String> sports = new ArrayList<>();
        sports.add("Tất cả");
        sports.addAll(db.getCoachSports());
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sports);
        spnSport.setAdapter(spnAdapter);

        // Adapter list
        adapter = new CoachSimpleAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        // load lần đầu
        loadData("Tất cả");

        spnSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData((String) parent.getItemAtPosition(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        return v;
    }

    private void loadData(String sport) {
        List<Coach> data = db.getCoachesBySport(sport);
        adapter.submit(data);
    }
}
