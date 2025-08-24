// admin/adapters/ProductAdminAdapter.java
package com.example.sportcenterapp.admin.adapters;

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
import com.example.sportcenterapp.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface OnItemClick { void onClick(Product p, int position); }

    private final List<Product> data = new ArrayList<>();
    private final Context ctx;
    private final OnItemClick onItemClick;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public AdminProductAdapter(Context ctx, OnItemClick onItemClick) {
        this.ctx = ctx; this.onItemClick = onItemClick;
    }

    public void submitList(List<Product> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public Product getItem(int position) { return data.get(position); }

    public void removeAt(int position) {
        if (position < 0 || position >= data.size()) return;
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreAt(Product p, int position) {
        data.add(position, p);
        notifyItemInserted(position);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_admin_product, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = data.get(pos);
        h.tvName.setText(p.name);
        h.tvPrice.setText(currency.format(p.price));
        h.tvStock.setText("Kho: " + p.stock);

        // Ưu tiên URL; nếu là tên drawable thì load drawable
        if (p.image != null && (p.image.startsWith("http") || p.image.startsWith("/"))) {
            Glide.with(ctx).load(p.image).placeholder(R.drawable.ic_product).into(h.ivImage);
        } else {
            int resId = ctx.getResources().getIdentifier(
                    p.image == null ? "" : p.image, "drawable", ctx.getPackageName());
            if (resId != 0) h.ivImage.setImageResource(resId);
            else h.ivImage.setImageResource(R.drawable.ic_product);
        }

        h.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(p, pos); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage; TextView tvName, tvPrice, tvStock;
        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
        }
    }
}
