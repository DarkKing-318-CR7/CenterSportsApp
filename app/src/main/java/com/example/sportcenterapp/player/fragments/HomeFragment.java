package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.BannerAdapter;
import com.example.sportcenterapp.adapters.PromotionAdapter;
import com.example.sportcenterapp.adapters.CourtAdapter;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.models.Promotion;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private TabLayout tabIndicator;
    private RecyclerView rvPromotions, rvFeaturedCourts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        tabIndicator = view.findViewById(R.id.tabIndicator);
        rvPromotions = view.findViewById(R.id.rvPromotions);
        rvFeaturedCourts = view.findViewById(R.id.rvHighlight);

        setupBanner();
        setupPromotions();
        setupFeaturedCourts();

        return view;
    }

    private void setupBanner() {
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.banner1);
        bannerImages.add(R.drawable.banner2);
        bannerImages.add(R.drawable.banner3);

        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        viewPagerBanner.setAdapter(bannerAdapter);

        new TabLayoutMediator(tabIndicator, viewPagerBanner, (tab, position) -> {}).attach();
    }

    private void setupPromotions() {
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion("Giảm 20% đặt sân buổi sáng", R.drawable.promo1));
        promotions.add(new Promotion("Mua 1 tặng 1 nước uống", R.drawable.promo2));
        promotions.add(new Promotion("Miễn phí thuê bóng", R.drawable.promo3));

        rvPromotions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPromotions.setAdapter(new PromotionAdapter(promotions));
    }

    private void setupFeaturedCourts() {
        List<Court> courts = new ArrayList<>();

        Court c1 = new Court();
        c1.name  = "Sân 7 người";
        c1.price = 250000;
        c1.sport = "Bóng đá";
        c1.image = ""; // để trống -> adapter sẽ dùng placeholder
        courts.add(c1);

        Court c2 = new Court();
        c2.name  = "Sân BC 01";
        c2.price = 150000;
        c2.sport = "Bóng chuyền";
        c2.image = "";
        courts.add(c2);

        Court c3 = new Court();
        c3.name  = "Cầu lông 01";
        c3.price = 120000;
        c3.sport = "Cầu lông";
        c3.image = "";
        courts.add(c3);

        rvFeaturedCourts.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // CourtAdapter cần 2 đối số: data + Actions
        rvFeaturedCourts.setAdapter(new CourtAdapter(courts, new CourtAdapter.Actions() {
            @Override public void onEdit(Court c) { /* Home chỉ hiển thị -> bỏ trống */ }
            @Override public void onDelete(Court c) { /* Home chỉ hiển thị -> bỏ trống */ }
        }));
    }

}
