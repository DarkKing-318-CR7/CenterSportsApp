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

/**
 * Dùng chung: Player & Admin
 * - Player: new CourtAdapter(list) hoặc CourtAdapter(list, listener, false)
 * - Admin : CourtAdapter(list, listener, true) -> tap item = Edit
 *
 * Hỗ trợ 2 layout:
 *  - item_court.xml (Player): imgCourt, tvCourtName, tvCourtPrice
 *  - item_admin_court.xml (Admin): tvName, tvMeta, tvPrice (card clickable/focusable)  // :contentReference[oaicite:1]{index=1}
 */
public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.VH> {

    public interface OnCourtAction {
        default void onClick(Court c) {}
        default void onEdit(Court c) {}
        default void onDelete(Court c) {}
    }

    private final List<Court> courts;
    private final OnCourtAction listener; // có thể null (Player đơn thuần)
    private final boolean isAdmin;

    // Player giữ nguyên cách dùng cũ
    public CourtAdapter(List<Court> courts) {
        this(courts, null, false);
    }

    // Dùng cho Player muốn bắt click, hoặc Admin (isAdmin=true)
    public CourtAdapter(List<Court> courts, OnCourtAction listener, boolean isAdmin) {
        this.courts = courts;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isAdmin ? R.layout.item_admin_court : R.layout.item_court;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v, isAdmin, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Court c = courts.get(position);

        // ---- Bind tên ----
        if (h.tvName != null) h.tvName.setText(c.getName());         // admin
        if (h.tvCourtName != null) h.tvCourtName.setText(c.getName()); // player

        // ---- Bind giá ----
        String priceText = ((int) c.getPrice()) + " đ/giờ";
        if (h.tvPrice != null) h.tvPrice.setText(priceText);            // admin
        if (h.tvCourtPrice != null) h.tvCourtPrice.setText(priceText);  // player

        // ---- Meta (admin) ----
        if (h.tvMeta != null) {
            String meta = c.getSport() + " • " + (c.Indoor() == 1 ? "indoor" : "outdoor") + " • " + c.getSurface();
            h.tvMeta.setText(meta);
        }

        // ---- Ảnh (nếu layout player có ImageView) ----
        int resId = 0;
        String img = c.getImage();
        if (img != null && !img.trim().isEmpty()) {
            resId = h.itemView.getContext().getResources()
                    .getIdentifier(img.trim(), "drawable",
                            h.itemView.getContext().getPackageName());
        }
        if (resId == 0) resId = R.drawable.placeholder_court;

        if (h.ivImage != null) h.ivImage.setImageResource(resId);


        // ---- Click: Admin = Edit ; Player = Click ----
        if (h.clickTarget != null) {
            if (h.isAdmin) {
                h.clickTarget.setOnClickListener(v -> { if (h.listener != null) h.listener.onEdit(c); });
                h.clickTarget.setOnLongClickListener(v -> { if (h.listener != null) h.listener.onEdit(c); return true; });
            } else {
                h.clickTarget.setOnClickListener(v -> { if (h.listener != null) h.listener.onClick(c); });
            }
        } else {
            // fallback
            if (h.isAdmin) {
                h.itemView.setOnClickListener(v -> { if (h.listener != null) h.listener.onEdit(c); });
                h.itemView.setOnLongClickListener(v -> { if (h.listener != null) h.listener.onEdit(c); return true; });
            } else {
                h.itemView.setOnClickListener(v -> { if (h.listener != null) h.listener.onClick(c); });
            }
        }
    }

    @Override public int getItemCount() { return courts.size(); }

    /* ---------- ViewHolder hỗ trợ 2 layout ---------- */
    static class VH extends RecyclerView.ViewHolder {
        // Player
        View  clickTarget;           // card root nếu tìm được
        ImageView ivImage;
        TextView tvCourtName, tvCourtPrice;

        // Admin
        TextView tvName, tvMeta, tvPrice;

        final boolean isAdmin;
        final OnCourtAction listener;

        VH(@NonNull View v, boolean isAdmin, OnCourtAction listener) {
            super(v);
            this.isAdmin = isAdmin;
            this.listener = listener;

            // Player ids (item_court.xml)
            ivImage     = v.findViewById(R.id.ivImage);
            tvCourtName  = v.findViewById(R.id.tvCourtName);
            tvCourtPrice = v.findViewById(R.id.tvCourtPrice);

            // Admin ids (item_admin_court.xml)
            tvName  = v.findViewById(R.id.tvName);
            tvMeta  = v.findViewById(R.id.tvMeta);
            tvPrice = v.findViewById(R.id.tvPrice);

            // Click target: nếu layout có card root riêng, có thể findViewById nó ở đây;
            // với file bạn gửi, chính MaterialCardView là root -> dùng itemView luôn
            clickTarget = itemView;
            clickTarget.setClickable(true);
            clickTarget.setFocusable(true);
        }
    }
}
