package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sportcenterapp.R;

public class HomeFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_home, container, false);
    }
}
