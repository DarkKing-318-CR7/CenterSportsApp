package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnAdd { void onAdd(Product p); }

    private final List<Product> items;
    private final Context context;
    @Nullable private final OnAdd onAdd;

    // Admin / nơi không cần nút "Thêm"
    public ProductAdapter(List<Product> items, Context context) {
        this(items, context, null);
    }

    // Player / nơi cần nút "Thêm"
    public ProductAdapter(List<Product> items, Context context, @Nullable OnAdd onAdd) {
        this.items = items;
        this.context = context;
        this.onAdd = onAdd;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = items.get(pos);
        h.tvName.setText(p.name);
        h.tvPrice.setText(String.format("%,.0fđ", p.price));

        // Các view có thể không tồn tại trong layout
        if (h.tvStock != null) h.tvStock.setText("Kho: " + p.stock);
        if (h.img != null) {
            int resId = context.getResources().getIdentifier(
                    (p.image == null || p.image.isEmpty()) ? "ic_product" : p.image,
                    "drawable",
                    context.getPackageName()
            );
            h.img.setImageResource(resId == 0 ? R.drawable.ic_product : resId);
        }

        if (h.btnAdd != null) {
            h.btnAdd.setOnClickListener(v -> {
                if (onAdd != null) onAdd.onAdd(p);
            });
            h.btnAdd.setVisibility(onAdd == null ? View.GONE : View.VISIBLE);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice,tvStock;
        Button btnAdd;
        VH(@NonNull View v) {
            super(v);
            img    = v.findViewById(R.id.img);      // <-- thêm
            tvName = v.findViewById(R.id.tvName);
            tvPrice= v.findViewById(R.id.tvPrice);
            tvStock =v.findViewById(R.id.tvStock);
            btnAdd = v.findViewById(R.id.btnAdd);

        }
    }

}