package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CartAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvTotal;
    private DatabaseHelper db;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_cart, c, false);

        rv = v.findViewById(R.id.rvCart);
        tvTotal = v.findViewById(R.id.tvTotal);
        Button btnCheckout = v.findViewById(R.id.btnCheckout);

        db = new DatabaseHelper(getContext());
        userId = new SessionManager(requireContext()).getUserId();

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setClipToPadding(false);

        // Đổi nhãn: không còn “Thanh toán” trong app
        btnCheckout.setText("Đặt hàng");
        btnCheckout.setOnClickListener(vw -> doCheckout());

        loadCart();
        return v;
    }

    private void loadCart() {
        final List<Object[]> cart = db.getCartItems(userId);

        rv.setAdapter(new CartAdapter(cart, (productId, desiredQty) -> {
            // Lấy tồn kho hiện tại
            int stock = 0;
            com.example.sportcenterapp.models.Product p = db.getProductById(productId);
            if (p != null) stock = p.stock;

            // Tìm qty hiện tại trong list cart (đủ dùng cho lần click này)
            int currentQty = 0;
            for (Object[] row : cart) {
                if ((int) row[0] == productId) { currentQty = (int) row[3]; break; }
            }

            // Kẹp lại số lượng (>=0 và <= stock)
            int newQty = Math.max(0, Math.min(desiredQty, stock));

            if (newQty == currentQty && desiredQty > currentQty) {
                // Người dùng bấm + nhưng đã chạm trần tồn kho
                Toast.makeText(getContext(), "Hết hàng hoặc đã đạt tối đa (" + stock + ")", Toast.LENGTH_SHORT).show();
            }

            db.updateCartQty(userId, productId, newQty);
            loadCart(); // refresh lại list + tổng tiền

            // cập nhật badge số lượng trên TopAppBar (nếu Activity có)
            if (getActivity() instanceof com.example.sportcenterapp.player.PlayerActivity) {
                ((com.example.sportcenterapp.player.PlayerActivity) getActivity()).refreshCartCount();
            }
        }));

        tvTotal.setText(String.format(Locale.getDefault(), "Tổng: %,.0fđ", db.getCartTotal(userId)));
    }


    /** Tạo Order (status=pending) + chuyển sang lịch sử đơn; KHÔNG thanh toán trong app */
    /** Tạo Order (status=pending) + trừ tồn kho; báo thiếu hàng nếu có */
    private void doCheckout() {
        if (db.getCartItems(userId).isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        long orderId = db.checkoutFromCart(userId);

        if (orderId == -2) {
            // DB trả -2 khi có sp hết hàng / không đủ số lượng
            Toast.makeText(getContext(),
                    "Một số sản phẩm đã hết hàng hoặc không đủ số lượng.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (orderId <= 0) {
            Toast.makeText(getContext(), "Đặt hàng thất bại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // OK
        loadCart();
        if (getActivity() instanceof com.example.sportcenterapp.player.PlayerActivity) {
            ((com.example.sportcenterapp.player.PlayerActivity) getActivity()).refreshCartCount();
        }
        Toast.makeText(getContext(), "Đã tạo đơn #" + orderId + ". Thanh toán tại quầy.", Toast.LENGTH_SHORT).show();

        // Điều hướng sang lịch sử đơn của người chơi (nếu bạn muốn)
        try {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new OrdersFragment())
                    .commit();
            com.google.android.material.appbar.MaterialToolbar tb = requireActivity().findViewById(R.id.topAppBar);
            if (tb != null) tb.setSubtitle("Lịch sử đơn hàng");
        } catch (Exception ignored) { }
    }

}
