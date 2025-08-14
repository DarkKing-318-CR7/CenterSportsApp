package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Coach;

import java.util.List;

public class CoachSimpleAdapter extends RecyclerView.Adapter<CoachSimpleAdapter.H> {
    private List<Coach> data;
    public CoachSimpleAdapter(List<Coach> d){ data=d; }
    public void submit(List<Coach> d){ this.data = d; notifyDataSetChanged(); }

    static class H extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvLevel, tvRate, tvBio;
        ImageView iv;
        ImageButton btnCall, btnSms, btnEmail, btnZalo;
        H(View v){
            super(v);
            tvName=v.findViewById(R.id.tvName);
            tvSport=v.findViewById(R.id.tvSport);
            tvLevel=v.findViewById(R.id.tvLevel);
            tvRate=v.findViewById(R.id.tvRate);
            tvBio=v.findViewById(R.id.tvBio);
            iv=v.findViewById(R.id.ivAvatar);
            btnCall=v.findViewById(R.id.btnCall);
            btnSms=v.findViewById(R.id.btnSms);
            btnEmail=v.findViewById(R.id.btnEmail);
        }
    }

    @NonNull
    @Override public H onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_coach_simple, p, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H h, int i) {
        Context ctx = h.itemView.getContext();
        Coach c = data.get(i);
        h.tvName.setText(c.name);
        h.tvSport.setText(c.sport);
        h.tvLevel.setText(c.level);
        if (c.ratePerHour > 0) {
            h.tvRate.setText(String.format("%,.0fđ/giờ", c.ratePerHour));
            h.tvRate.setVisibility(View.VISIBLE);
        } else h.tvRate.setVisibility(View.GONE);
        h.tvBio.setText(c.bio == null ? "" : c.bio);

        // avatar nếu là tên drawable
        if (c.avatar != null && !c.avatar.isEmpty()) {
            int resId = ctx.getResources().getIdentifier(c.avatar, "drawable", ctx.getPackageName());
            if (resId != 0) h.iv.setImageResource(resId);
        }

        // Liên hệ
        h.btnCall.setOnClickListener(v -> {
            if (c.phone != null && !c.phone.isEmpty()) {
                Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.phone));
                try { ctx.startActivity(it); } catch (Exception e) {
                    Toast.makeText(ctx, "Không mở được ứng dụng gọi điện", Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(ctx, "HLV chưa có số điện thoại", Toast.LENGTH_SHORT).show();
        });
        h.btnSms.setOnClickListener(v -> {
            if (c.phone != null && !c.phone.isEmpty()) {
                Intent it = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + c.phone));
                try { ctx.startActivity(it); } catch (Exception e) {
                    Toast.makeText(ctx, "Không mở được SMS", Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(ctx, "HLV chưa có số điện thoại", Toast.LENGTH_SHORT).show();
        });
        h.btnEmail.setOnClickListener(v -> {
            if (c.email != null && !c.email.isEmpty()) {
                Intent it = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + c.email));
                it.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ HLV " + c.name);
                try { ctx.startActivity(Intent.createChooser(it, "Gửi email")); } catch (Exception e) {
                    Toast.makeText(ctx, "Không mở được Email", Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(ctx, "HLV chưa có email", Toast.LENGTH_SHORT).show();
        });

    }

    @Override public int getItemCount(){ return data==null?0:data.size(); }
}
