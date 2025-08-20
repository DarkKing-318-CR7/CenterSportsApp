package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnAdd    { void onAdd(Product p); }
    public interface OnEdit   { void onEdit(Product p); }
    public interface OnDelete { void onDelete(Product p); }

    private final List<Product> items;
    private final Context context;

    @Nullable private final OnAdd onAdd;       // Player
    @Nullable private final OnEdit onEdit;     // Admin
    @Nullable private final OnDelete onDelete; // Admin
    private final boolean isAdmin;

    // Player giữ nguyên
    public ProductAdapter(List<Product> items, Context context) {
        this(items, context, null, null, null, false);
    }

    // Player muốn bắt nút “Thêm giỏ”
    public ProductAdapter(List<Product> items, Context context, @Nullable OnAdd onAdd) {
        this(items, context, onAdd, null, null, false);
    }

    // Admin
    public ProductAdapter(List<Product> items, Context context,
                          @Nullable OnAdd onAdd,
                          @Nullable OnEdit onEdit,
                          @Nullable OnDelete onDelete,
                          boolean isAdmin) {
        this.items = items;
        this.context = context;
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.isAdmin = isAdmin;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isAdmin ? R.layout.item_admin_product : R.layout.item_product;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);

        if (h.tvName != null)  h.tvName.setText(p.getName());
        if (h.tvPrice != null) h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", p.getPrice()));
        if (h.tvStock != null) h.tvStock.setText("Kho: " + p.getStock());

        // --- LOAD ẢNH an toàn: tên drawable hoặc URL (nếu bạn thêm Glide) ---
        ImageView target = h.ivImage; // đã bắt nhiều khả năng id trong VH
        if (target != null) {
            String img = p.getImage();
            if (img != null && !img.trim().isEmpty()) {
                if (img.startsWith("http")) {
                    // Nếu dùng ảnh URL, bật Glide (khuyến nghị). Nếu chưa add Glide, dùng placeholder tạm.
                    // Glide.with(h.itemView).load(img).placeholder(R.drawable.placeholder_product).into(target);
                    target.setImageResource(R.drawable.placeholder_product);
                } else {
                    int resId = h.itemView.getContext().getResources()
                            .getIdentifier(img.trim(), "drawable", h.itemView.getContext().getPackageName());
                    target.setImageResource(resId != 0 ? resId : R.drawable.placeholder_product);
                }
            } else {
                target.setImageResource(R.drawable.placeholder_product);
            }
        }
        // --- Hành vi theo role ---
        if (isAdmin) {
            // Click item = Sửa
            h.itemView.setOnClickListener(v -> { if (onEdit != null) onEdit.onEdit(p); });
            if (h.btnEdit   != null) h.btnEdit.setOnClickListener(v -> { if (onEdit   != null) onEdit.onEdit(p); });
            if (h.btnDelete != null) h.btnDelete.setOnClickListener(v -> { if (onDelete != null) onDelete.onDelete(p); });
        } else {
            // Player: chỉ nút “Thêm giỏ” mới thêm
            if (h.btnAdd != null) h.btnAdd.setOnClickListener(v -> { if (onAdd != null) onAdd.onAdd(p); });
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;            // sẽ gán vào bất kỳ id ảnh nào tìm thấy
        TextView tvName, tvPrice, tvStock;
        ImageButton btnEdit, btnDelete; // admin
        View btnAdd;                   // player

        VH(@NonNull View v) {
            super(v);
            // --- bind text chung ---
            tvName   = v.findViewById(R.id.tvName);
            tvPrice  = v.findViewById(R.id.tvPrice);
            tvStock  = v.findViewById(R.id.tvStock);

            // --- tìm ImageView theo nhiều ID khả dụng ---
            ImageView img = v.findViewById(R.id.ivImage);
            if (img == null) img = v.findViewById(R.id.img);
            if (img == null) img = v.findViewById(R.id.image);
            ivImage = img; // có thể null, onBind đã check

            // --- nút admin (có thể null nếu layout player) ---
            btnEdit   = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);

            // --- nút player (có thể null nếu layout admin) ---
            View addBtn = v.findViewById(R.id.btnAdd);
            if (addBtn == null) addBtn = v.findViewById(R.id.btnAdd);
            if (addBtn == null) addBtn = v.findViewById(R.id.btnAdd);
            btnAdd = addBtn;
        }
    }
}
