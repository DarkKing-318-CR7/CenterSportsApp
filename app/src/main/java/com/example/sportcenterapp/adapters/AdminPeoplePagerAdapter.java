package com.example.sportcenterapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sportcenterapp.admin.fragments.UsersFragment;
import com.example.sportcenterapp.admin.fragments.CoachesFragment;

public class AdminPeoplePagerAdapter extends FragmentStateAdapter {

    // Truyền Fragment cha đang chứa ViewPager2
    public AdminPeoplePagerAdapter(@NonNull Fragment parent) {
        super(parent);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tab 0: Users, Tab 1: Coaches
        if (position == 1) {
            return new CoachesFragment();
        }
        return new UsersFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
