// com.example.sportcenterapp.adapters.CoachSimpleAdapter
package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Coach;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CoachSimpleAdapter extends RecyclerView.Adapter<CoachSimpleAdapter.H> {

    @FunctionalInterface
    public interface OnItemClick { void onClick(Coach coach); }

    private final List<Coach> data = new ArrayList<>();
    @Nullable private final OnItemClick onItemClick;

    // Admin: truyền callback để sửa; Player: có thể truyền null
    public CoachSimpleAdapter(@Nullable List<Coach> init, @Nullable OnItemClick onItemClick) {
        if (init != null) data.addAll(init);
        this.onItemClick = onItemClick;
    }
    // Player (không callback)
    public CoachSimpleAdapter(@Nullable List<Coach> init) { this(init, null); }

    public void submit(@Nullable List<Coach> d) {
        data.clear();
        if (d != null) data.addAll(d);
        notifyDataSetChanged();
    }

    static class H extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvLevel, tvRate, tvBio;
        ImageView iv;
        ImageButton btnCall, btnSms, btnEmail;
        H(View v){
            super(v);
            tvName  = v.findViewById(R.id.tvName);
            tvSport = v.findViewById(R.id.tvSport);
            tvLevel = v.findViewById(R.id.tvLevel);
            tvRate  = v.findViewById(R.id.tvRate);
            tvBio   = v.findViewById(R.id.tvBio);
            iv      = v.findViewById(R.id.ivAvatar);
            btnCall = v.findViewById(R.id.btnCall);
            btnSms  = v.findViewById(R.id.btnSms);
            btnEmail= v.findViewById(R.id.btnEmail);
        }
    }

    @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_coach_simple, p, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H h, int i) {
        Context ctx = h.itemView.getContext();
        Coach c = data.get(i);

        h.tvName.setText(n(c.getName()));
        h.tvSport.setText(n(c.getSport()));
        h.tvLevel.setText(n(c.getLevel()));
        if (c.getRatePerHour() > 0) {
            h.tvRate.setText(String.format(Locale.getDefault(), "%,.0fđ/giờ", c.getRatePerHour()));
            h.tvRate.setVisibility(View.VISIBLE);
        } else h.tvRate.setVisibility(View.GONE);
        h.tvBio.setText(n(c.getBio()));

        int resId = 0;
        String avatar = c.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            resId = ctx.getResources().getIdentifier(avatar, "drawable", ctx.getPackageName());
        }
        h.iv.setImageResource(resId != 0 ? resId : R.drawable.ic_person);

        h.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(c); });

        h.btnCall.setOnClickListener(v -> {
            String phone = c.getPhone();
            if (phone != null && !phone.isEmpty()) {
                try { ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))); }
                catch (Exception e) { Toast.makeText(ctx, "Không mở được ứng dụng gọi", Toast.LENGTH_SHORT).show(); }
            } else Toast.makeText(ctx, "HLV chưa có SĐT", Toast.LENGTH_SHORT).show();
        });
        h.btnSms.setOnClickListener(v -> {
            String phone = c.getPhone();
            if (phone != null && !phone.isEmpty()) {
                try { ctx.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone))); }
                catch (Exception e) { Toast.makeText(ctx, "Không mở được SMS", Toast.LENGTH_SHORT).show(); }
            } else Toast.makeText(ctx, "HLV chưa có SĐT", Toast.LENGTH_SHORT).show();
        });
        h.btnEmail.setOnClickListener(v -> {
            String email = c.getEmail();
            if (email != null && !email.isEmpty()) {
                Intent it = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                it.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ HLV " + n(c.getName()));
                try { ctx.startActivity(Intent.createChooser(it, "Gửi email")); }
                catch (Exception e) { Toast.makeText(ctx, "Không mở được Email", Toast.LENGTH_SHORT).show(); }
            } else Toast.makeText(ctx, "HLV chưa có email", Toast.LENGTH_SHORT).show();
        });
    }

    @Override public int getItemCount() { return data.size(); }
    private static String n(String s){ return s==null?"":s; }
}
