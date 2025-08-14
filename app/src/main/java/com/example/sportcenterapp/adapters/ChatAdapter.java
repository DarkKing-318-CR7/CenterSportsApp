package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_PLAYER = 0;
    private static final int TYPE_ADMIN = 1;
    private List<ChatMessage> data;
    public ChatAdapter(List<ChatMessage> data){ this.data = data; }

    @Override public int getItemViewType(int pos) {
        return "player".equalsIgnoreCase(data.get(pos).senderRole) ? TYPE_PLAYER : TYPE_ADMIN;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        VH(View v){ super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        int layout = (vt == TYPE_PLAYER) ? R.layout.item_chat_player : R.layout.item_chat_admin;
        View v = LayoutInflater.from(p.getContext()).inflate(layout, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int pos) {
        VH h = (VH) vh;
        ChatMessage m = data.get(pos);
        h.tvMsg.setText(m.message);
        h.tvTime.setText(m.timestamp.substring(11,16)); // chỉ HH:mm
    }

    @Override public int getItemCount(){ return data.size(); }
}
