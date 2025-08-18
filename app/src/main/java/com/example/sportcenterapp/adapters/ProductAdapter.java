package com.example.sportcenterapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;

import java.io.File;
import java.util.List;
import java.util.Locale;

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
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", p.price));

        if (h.tvStock != null) h.tvStock.setText("Kho: " + p.stock);

        // LOAD ẢNH: chấp nhận drawable name / absolute path / content://
        if (h.ivImage != null) loadImage(context, h.ivImage, p.image);

        if (h.btnAdd != null) {
            h.btnAdd.setVisibility(onAdd == null ? View.GONE : View.VISIBLE);
            h.btnAdd.setEnabled(p.stock > 0);
            h.btnAdd.setOnClickListener(v -> { if (onAdd != null) onAdd.onAdd(p); });
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvPrice;
        @Nullable final TextView tvStock;   // optional trong layout
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

    /** Helper load ảnh từ drawable name / file path / content URI */
    private static void loadImage(Context ctx, ImageView iv, @Nullable String img) {
        if (img == null || img.trim().isEmpty()) {
            iv.setImageResource(R.drawable.ic_product);
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
            // fallback: drawable
            int resId = ctx.getResources().getIdentifier(img, "drawable", ctx.getPackageName());
            iv.setImageResource(resId != 0 ? resId : R.drawable.ic_product);
        } catch (Exception e) {
            iv.setImageResource(R.drawable.ic_product);
        }
    }
}
