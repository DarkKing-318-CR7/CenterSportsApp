package com.example.sportcenterapp.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.net.ApiClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
    public interface OnAction {
        void onAddToCart(Product p);
        void onClick(Product p); // (nếu muốn mở chi tiết)
    }

    private final List<Product> ds;
    private final OnAction cb;
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi","VN"));

    public ProductAdapter(List<Product> ds, OnAction cb){ this.ds = ds; this.cb = cb; }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image; TextView tvName, tvPrice; Button btnAdd;
        VH(View v){
            super(v);
            image   = v.findViewById(R.id.image);
            tvName  = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnAdd  = v.findViewById(R.id.btnAdd);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_product, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = ds.get(pos);
        h.tvName.setText(p.name != null ? p.name : "");
        h.tvPrice.setText(money.format(p.price) + "đ");

        String img = p.image;
        if (img != null && !img.isEmpty()) {
            if (!img.startsWith("http")) img = ApiClient.BASE_URL + (img.startsWith("/") ? img.substring(1) : img);
            Glide.with(h.image.getContext())
                    .load(img)
                    .placeholder(R.drawable.placeholder_court)
                    .error(R.drawable.placeholder_court)
                    .into(h.image);
        } else {
            h.image.setImageResource(R.drawable.placeholder_court);
        }

        h.itemView.setOnClickListener(v -> { if (cb!=null) cb.onClick(p); });
        h.btnAdd.setOnClickListener(v -> { if (cb!=null) cb.onAddToCart(p); });
    }

    @Override public int getItemCount() { return ds.size(); }
}
