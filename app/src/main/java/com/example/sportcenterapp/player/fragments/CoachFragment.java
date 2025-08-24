package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CoachSimpleAdapter;
import com.example.sportcenterapp.models.Coach;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoachFragment extends Fragment {
    private RecyclerView rv;
    private Spinner spnSport;

    private final List<Coach> all = new ArrayList<>();
    private CoachSimpleAdapter adapter;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_coach_simple, parent, false);

        rv = v.findViewById(R.id.rvCoaches);
        spnSport = v.findViewById(R.id.spnSport);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CoachSimpleAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        api = ApiClient.build().create(ApiService.class);

        // gọi API lấy HLV
        loadCoachesFromApi();

        spnSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sport = String.valueOf(parent.getItemAtPosition(position));
                filterAndShow(sport);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        return v;
    }

    private void loadCoachesFromApi() {
        // truyền null hoặc "" để lấy tất cả
        api.getCoaches(null).enqueue(new Callback<List<Coach>>() {
            @Override public void onResponse(@NonNull Call<List<Coach>> call, @NonNull Response<List<Coach>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(getContext(), "Không tải được HLV", Toast.LENGTH_SHORT).show();
                    return;
                }
                all.clear();
                all.addAll(res.body());

                // build list môn cho Spinner
                Set<String> set = new LinkedHashSet<>();
                set.add("Tất cả");
                for (Coach c : all) {
                    if (c.getSport() != null && !c.getSport().trim().isEmpty()) {
                        set.add(c.getSport().trim());
                    }
                }
                ArrayAdapter<String> spnAdapter =
                        new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(set));
                spnSport.setAdapter(spnAdapter);

                // hiển thị tất cả
                filterAndShow("Tất cả");
            }

            @Override public void onFailure(@NonNull Call<List<Coach>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndShow(String sport) {
        List<Coach> showing = new ArrayList<>();
        boolean isAll = "Tất cả".equalsIgnoreCase(sport);
        for (Coach c : all) {
            if (isAll) showing.add(c);
            else if (sport.equalsIgnoreCase(String.valueOf(c.getSport()))) showing.add(c);
        }
        adapter.submit(showing);
    }
}
