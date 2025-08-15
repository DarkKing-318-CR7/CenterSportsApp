package com.example.sportcenterapp.admin.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InventoryPagerAdapter extends FragmentStateAdapter {

    public InventoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new ProductsFragment(); // bạn đã có
        return new CourtsFragment(); // bạn đã có
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
