package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnAdd { void add(Product p); }

    private final List<Product> data;
    private final OnAdd cb;

    public ProductAdapter(List<Product> data, OnAdd cb) {
        this.data = data;
        this.cb = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice;
        Button btnAdd;

        VH(@NonNull View item) {
            super(item);
            // KHỚP ID trong item_product.xml (img, tvName, tvPrice, btnAdd)
            img    = item.findViewById(R.id.img);
            tvName = item.findViewById(R.id.tvName);
            tvPrice= item.findViewById(R.id.tvPrice);
            btnAdd = item.findViewById(R.id.btnAdd);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = data.get(position);
        h.tvName.setText(p.name);
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", p.price));

        int resId = h.img.getResources().getIdentifier(
                (p.image == null || p.image.isEmpty()) ? "placeholder_court" : p.image,
                "drawable",
                h.img.getContext().getPackageName()
        );
        h.img.setImageResource(resId == 0 ? R.drawable.placeholder_court : resId);

        h.btnAdd.setOnClickListener(v -> { if (cb != null) cb.add(p); });
    }

    @Override
    public int getItemCount() { return data.size(); }
}
