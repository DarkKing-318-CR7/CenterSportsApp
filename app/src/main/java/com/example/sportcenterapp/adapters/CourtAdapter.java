package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Court;

import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.ViewHolder> {

    public interface OnCourtClickListener {
        void onCourtClick(Court court);
    }

    private Context context;
    private List<Court> courtList;
    private OnCourtClickListener listener;

    public CourtAdapter(Context context, List<Court> courtList, OnCourtClickListener listener) {
        this.context = context;
        this.courtList = courtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_court, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtAdapter.ViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.tvName.setText(court.getName());
        holder.tvSport.setText(court.getSport());
        holder.tvStatus.setText("Trạng thái: " + court.getStatus());

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourtClick(court);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvStatus;
        Button btnBook;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourtName);
            tvSport = itemView.findViewById(R.id.tvSport);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}
