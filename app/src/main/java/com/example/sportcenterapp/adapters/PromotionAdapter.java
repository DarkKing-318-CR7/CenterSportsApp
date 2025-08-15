package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Promotion;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private List<Promotion> promotions;

    public PromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promo = promotions.get(position);
        holder.imgPromo.setImageResource(promo.getImageResId());
        holder.tvPromoTitle.setText(promo.getTitle());
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPromo;
        TextView tvPromoTitle;
        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPromo = itemView.findViewById(R.id.imgPromo);
            tvPromoTitle = itemView.findViewById(R.id.tvPromoTitle);
        }
    }
}
