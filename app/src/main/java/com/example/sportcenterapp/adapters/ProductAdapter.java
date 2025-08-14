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
        if (h.ivImage != null) {
            int resId = context.getResources().getIdentifier(
                    (p.image == null || p.image.isEmpty()) ? "ic_product" : p.image,
                    "drawable",
                    context.getPackageName()
            );
            h.ivImage.setImageResource(resId == 0 ? R.drawable.ic_product : resId);
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
        final TextView tvName, tvPrice;
        @Nullable final TextView tvStock;   // optional
        @Nullable final ImageView ivImage;  // optional
        @Nullable final Button btnAdd;      // optional

        VH(@NonNull View itemView) {
            super(itemView);
            tvName  = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            TextView tmpStock;
            try { tmpStock = itemView.findViewById(R.id.tvStock); } catch (Exception e) { tmpStock = null; }
            tvStock = tmpStock;

            ImageView tmpIv;
            try { tmpIv = itemView.findViewById(R.id.ivImage); } catch (Exception e) { tmpIv = null; }
            ivImage = tmpIv;

            Button tmpAdd;
            try { tmpAdd = itemView.findViewById(R.id.btnAdd); } catch (Exception e) { tmpAdd = null; }
            btnAdd = tmpAdd;
        }
    }
}
