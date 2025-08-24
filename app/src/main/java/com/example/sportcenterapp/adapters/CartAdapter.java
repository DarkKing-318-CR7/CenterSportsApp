package com.example.sportcenterapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.utils.CartStore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    // Callback dùng trong CartFragment
    public interface OnAction {
        void onInc(int pos);
        void onDec(int pos);
        void onRemove(int pos);
    }

    private final List<CartStore.Item> ds;
    private final OnAction cb;
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi","VN"));

    public CartAdapter(List<CartStore.Item> ds, OnAction cb) {
        this.ds = ds;
        this.cb = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvQty;
        Button btnMinus, btnPlus, btnRemove; // btnRemove có thể null nếu layout không có
        VH(@NonNull View v){
            super(v);
            img       = v.findViewById(R.id.img);
            tvName    = v.findViewById(R.id.tvName);
            tvPrice   = v.findViewById(R.id.tvPrice);
            tvQty     = v.findViewById(R.id.tvQty);
            btnMinus  = v.findViewById(R.id.btnMinus);
            btnPlus   = v.findViewById(R.id.btnPlus);
            btnRemove = v.findViewById(R.id.btnRemove); // safe null
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_cart, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CartStore.Item it = ds.get(pos);
        Product p = it.p;
        int qty = it.qty;

        h.tvName.setText(p.name != null ? p.name : "");
        h.tvPrice.setText(money.format(Math.round(p.price)) + "đ");
        h.tvQty.setText(String.valueOf(qty));

        // Load ảnh (nếu server trả path tương đối thì ghép BASE_URL)
        String img = p.image;
        if (img != null && !img.isEmpty()) {
            if (!img.startsWith("http")) img = ApiClient.BASE_URL + (img.startsWith("/") ? img.substring(1) : img);
            Glide.with(h.img.getContext())
                    .load(img)
                    .placeholder(R.drawable.placeholder_court)
                    .error(R.drawable.placeholder_court)
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.placeholder_court);
        }

        h.btnPlus.setOnClickListener(v -> { if (cb!=null) cb.onInc(h.getBindingAdapterPosition()); });
        h.btnMinus.setOnClickListener(v -> { if (cb!=null) cb.onDec(h.getBindingAdapterPosition()); });

        if (h.btnRemove != null) {
            h.btnRemove.setOnClickListener(v -> { if (cb!=null) cb.onRemove(h.getBindingAdapterPosition()); });
        } else {
            // Nếu layout không có nút remove, cho long-press để xóa
            h.itemView.setOnLongClickListener(v -> {
                if (cb!=null) cb.onRemove(h.getBindingAdapterPosition());
                return true;
            });
        }
    }

    @Override
    public int getItemCount() { return ds.size(); }
}
