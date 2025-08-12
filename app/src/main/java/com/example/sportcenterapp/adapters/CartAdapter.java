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

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnQtyChange { void change(int productId, int newQty); }

    private final List<Object[]> data; // [product_id, name, price, qty, image]
    private final OnQtyChange cb;

    public CartAdapter(List<Object[]> data, OnQtyChange cb){
        this.data = data; this.cb = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvQty;
        Button btnMinus, btnPlus;
        VH(@NonNull View v){
            super(v);
            img = v.findViewById(R.id.img);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvQty = v.findViewById(R.id.tvQty);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_cart, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Object[] it = data.get(pos);
        int productId = (int) it[0];
        String name = (String) it[1];
        double price = (double) it[2];
        int qty = (int) it[3];
        String imgName = (String) it[4];

        h.tvName.setText(name);
        h.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", price));
        h.tvQty.setText(String.valueOf(qty));

        int resId = h.img.getResources().getIdentifier(
                (imgName == null || imgName.isEmpty()) ? "placeholder_court" : imgName,
                "drawable", h.img.getContext().getPackageName());
        h.img.setImageResource(resId == 0 ? R.drawable.placeholder_court : resId);

        h.btnMinus.setOnClickListener(v -> {
            int newQty = qty - 1;
            if (cb != null) cb.change(productId, newQty);
        });
        h.btnPlus.setOnClickListener(v -> {
            int newQty = qty + 1;
            if (cb != null) cb.change(productId, newQty);
        });
    }

    @Override public int getItemCount(){ return data.size(); }
}
