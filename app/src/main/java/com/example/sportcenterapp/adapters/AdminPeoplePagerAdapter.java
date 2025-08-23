package com.example.sportcenterapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sportcenterapp.admin.fragments.CoachesFragment;
import com.example.sportcenterapp.admin.fragments.UsersFragment;

public class AdminPeoplePagerAdapter extends FragmentStateAdapter {
    public AdminPeoplePagerAdapter(@NonNull Fragment parent) { super(parent); }
    @NonNull @Override public Fragment createFragment(int position) {
        return position == 0 ? new UsersFragment() : new CoachesFragment();
    }
    @Override public int getItemCount() { return 2; }
}

