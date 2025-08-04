package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Court;

import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private Context context;
    private List<Court> courtList;

    public CourtAdapter(Context context, List<Court> courtList) {
        this.context = context;
        this.courtList = courtList;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.tvName.setText(court.getName());
        holder.tvSport.setText("Môn: " + court.getSport());
        holder.tvStatus.setText("Trạng thái: " + court.getStatus());

        if (court.getStatus().equals("available")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // xanh
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // đỏ
        }

        holder.btnBook.setOnClickListener(v -> {
            if (bookClickListener != null) {
                bookClickListener.onBookClick(court);
            }
        });

    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvStatus,btnBook;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourtName);
            tvSport = itemView.findViewById(R.id.tvCourtSport);
            tvStatus = itemView.findViewById(R.id.tvCourtStatus);
            btnBook = itemView.findViewById(R.id.btnBookCourt);
        }
    }

    public interface OnBookClickListener {
        void onBookClick(Court court);
    }

    private OnBookClickListener bookClickListener;

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.bookClickListener = listener;
    }


}

