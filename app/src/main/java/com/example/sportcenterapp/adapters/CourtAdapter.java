package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Court;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.VH> {

    private final List<Court> data;
    private final Context ctx;

    public CourtAdapter(List<Court> data, Context ctx) {
        this.data = data; this.ctx = ctx;
    }

    static class VH extends RecyclerView.ViewHolder {
        @Nullable ImageView img;
        @Nullable TextView tvName, tvDesc, tvPrice;

        VH(@NonNull View v) {
            super(v);
            String pkg = v.getContext().getPackageName();

            // Tìm linh hoạt theo nhiều khả năng id để không vướng lỗi biên dịch
            img     = findImage(v, pkg, "img", "ivImage", "imageView");
            tvName  = findText (v, pkg, "tvName", "tvTitle", "title");
            tvDesc  = findText (v, pkg, "tvDesc", "tvSubtitle", "tvInfo", "subtitle", "desc");
            tvPrice = findText (v, pkg, "tvPrice", "tvMoney", "price");
        }

        @Nullable
        private static TextView findText(View v, String pkg, String... names) {
            for (String n : names) {
                int id = v.getResources().getIdentifier(n, "id", pkg);
                if (id != 0) return v.findViewById(id);
            }
            return null;
        }

        @Nullable
        private static ImageView findImage(View v, String pkg, String... names) {
            for (String n : names) {
                int id = v.getResources().getIdentifier(n, "id", pkg);
                if (id != 0) return v.findViewById(id);
            }
            return null;
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt){
        // Dùng đúng layout item sân của bạn
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_court, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Court c = data.get(pos);

        if (h.tvName  != null) h.tvName.setText(c.name);

        if (h.tvDesc != null) {
            String indoor = c.indoor == 1 ? "Trong nhà" : "Ngoài trời";
            String more = (c.surface != null && !c.surface.isEmpty()) ? " • " + c.surface : "";
            h.tvDesc.setText((c.sport != null ? c.sport : "") + " • " + indoor + more);
        }

        if (h.tvPrice != null) {
            h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ/giờ", c.price));
        }

        if (h.img != null) loadImage(ctx, h.img, c.image);
    }

    @Override public int getItemCount(){ return data.size(); }

    private static void loadImage(Context ctx, ImageView iv, @Nullable String img) {
        if (img == null || img.trim().isEmpty()) {
            iv.setImageResource(R.drawable.placeholder_court);
            return;
        }
        img = img.trim();
        try {
            if (img.startsWith("content://") || img.startsWith("file://")) {
                iv.setImageURI(Uri.parse(img));
                return;
            }
            if (img.startsWith("/")) { // absolute path
                iv.setImageURI(Uri.fromFile(new File(img)));
                return;
            }
            // drawable name
            int resId = ctx.getResources().getIdentifier(img, "drawable", ctx.getPackageName());
            iv.setImageResource(resId != 0 ? resId : R.drawable.placeholder_court);
        } catch (Exception e) {
            iv.setImageResource(R.drawable.placeholder_court);
        }
    }
}
