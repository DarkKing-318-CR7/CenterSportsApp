package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.net.ApiClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dùng chung: Player & Admin
 * - Player: new CourtAdapter(list) hoặc CourtAdapter(list, listener, false)
 * - Admin : CourtAdapter(list, listener, true) -> tap item = Edit
 *
 * Hỗ trợ 2 layout:
 *  - item_court.xml (Player): imgCourt, tvCourtName, tvCourtPrice
 *  - item_admin_court.xml (Admin): tvName, tvMeta, tvPrice (card clickable/focusable)  // :contentReference[oaicite:1]{index=1}
 */
// imports giữ nguyên, nhớ đã import Glide

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.VH> {

    public interface Actions { void onEdit(Court c); void onDelete(Court c); }

    private final List<Court> ds;
    private final Actions actions;

    public CourtAdapter(List<Court> ds, Actions actions) {
        this.ds = ds;
        this.actions = actions;
    }

    static class VH extends RecyclerView.ViewHolder {
        View item;
        ImageView iv;
        TextView tvName, tvMeta, tvPrice;
        VH(View v) {
            super(v);
            item    = v;
            iv      = v.findViewById(R.id.ivImage);
            tvName  = v.findViewById(R.id.tvName);
            tvMeta  = v.findViewById(R.id.tvMeta);
            tvPrice = v.findViewById(R.id.tvPrice);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_court, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Court c = ds.get(pos);

        // Tên
        h.tvName.setText(c.name != null ? c.name : "—");

        // Meta: sport • surface • indoor/outdoor
        StringBuilder meta = new StringBuilder();
        if (c.sport != null && !c.sport.isEmpty()) meta.append(c.sport);
        if (c.surface != null && !c.surface.isEmpty())
            meta.append(meta.length() > 0 ? " • " : "").append(c.surface);
        meta.append(meta.length() > 0 ? " • " : "")
                .append(c.indoor == 1 ? "indoor" : "outdoor");
        h.tvMeta.setText(meta.toString());

        // Giá
        double price = c.price != 0 ? c.price : c.price; // tuỳ model
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        h.tvPrice.setText(nf.format(price) + "đ/giờ");

        // Ảnh
        String img = c.image;
        if (img != null && !img.isEmpty()) {
            if (!img.startsWith("http")) {
                // Ghép BASE_URL nếu server trả đường dẫn tương đối: uploads/xxx.jpg
                img = ApiClient.BASE_URL + (img.startsWith("/") ? img.substring(1) : img);
            }
            Glide.with(h.iv.getContext())
                    .load(img)
                    .placeholder(R.drawable.placeholder_court)
                    .error(R.drawable.placeholder_court)
                    .into(h.iv);
        } else {
            h.iv.setImageResource(R.drawable.placeholder_court);
        }

        // Click -> edit
        h.item.setOnClickListener(v -> { if (actions != null) actions.onEdit(c); });
        // (Nếu cần xoá bằng nút trong item thì thêm ở đây và gọi actions.onDelete(c))
    }

    @Override public int getItemCount() { return ds != null ? ds.size() : 0; }
}