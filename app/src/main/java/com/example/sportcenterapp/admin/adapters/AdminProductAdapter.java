package com.example.sportcenterapp.admin.adapters;

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
import com.example.sportcenterapp.net.ApiClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface OnAction {
        void onEdit(Product p);
        void onDelete(Product p);
    }

    private final List<Product> ds;
    private final OnAction cb;
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));

    public AdminProductAdapter(List<Product> ds, OnAction cb) {
        this.ds = ds;
        this.cb = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvStock;
        VH(@NonNull View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivImage);   // trong item_admin_product.xml
            tvName  = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvStock = v.findViewById(R.id.tvStock);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_product, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = ds.get(pos);
        h.tvName.setText(p.name != null ? p.name : "");
        h.tvPrice.setText(money.format(Math.round(p.price)) + "đ");
        h.tvStock.setText("Tồn: " + p.stock);

        String img = p.image;
        if (img != null && !img.isEmpty()) {
            if (!img.startsWith("http")) {
                img = ApiClient.BASE_URL + (img.startsWith("/") ? img.substring(1) : img);
            }
            Glide.with(h.ivImage.getContext())
                    .load(img)
                    .placeholder(R.drawable.placeholder_court)
                    .error(R.drawable.placeholder_court)
                    .into(h.ivImage);
        } else {
            h.ivImage.setImageResource(R.drawable.placeholder_court);
        }

        // Click để sửa
        h.itemView.setOnClickListener(v -> { if (cb != null) cb.onEdit(p); });
        // Long-click để xoá (vuốt bạn đã enable ở Fragment; long-click là “đường lùi”)
        h.itemView.setOnLongClickListener(v -> { if (cb != null) cb.onDelete(p); return true; });
    }

    @Override public int getItemCount() { return ds.size(); }
}
