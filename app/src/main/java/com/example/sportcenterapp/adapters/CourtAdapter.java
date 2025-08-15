package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Court;

import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courts;

    public CourtAdapter(List<Court> courts) {
        this.courts = courts;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court c = courts.get(position);
        holder.tvCourtName.setText(c.getName());
        holder.tvCourtPrice.setText(((int)c.getPrice()) + " đ/giờ");

        Context ctx = holder.itemView.getContext();
        String imgName = c.getImage();
        int resId = 0;
        if (imgName != null && !imgName.trim().isEmpty()) {
            resId = ctx.getResources().getIdentifier(imgName.trim(), "drawable", ctx.getPackageName());
        }
        if (resId == 0) resId = R.drawable.placeholder_court; // ảnh dự phòng
        holder.imgCourt.setImageResource(resId);
    }




    @Override
    public int getItemCount() {
        return courts.size();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCourt;
        TextView tvCourtName, tvCourtPrice;
        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourt = itemView.findViewById(R.id.imgCourt);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvCourtPrice = itemView.findViewById(R.id.tvCourtPrice);
        }
    }
}
