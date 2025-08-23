package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.AdminPeoplePagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminPeopleFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_people, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TabLayout tab = v.findViewById(R.id.tabPeople);
        ViewPager2 pager = v.findViewById(R.id.pagerPeople);
        pager.setAdapter(new AdminPeoplePagerAdapter(this));

        new com.google.android.material.tabs.TabLayoutMediator(tab, pager,
                (t, pos) -> t.setText(pos == 0 ? "Tài khoản" : "HLV")
        ).attach();
    }
}

