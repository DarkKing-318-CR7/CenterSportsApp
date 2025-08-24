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
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.CartStore;
import com.example.sportcenterapp.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvTotal, tvEmpty;
    private Button btnCheckout;

    private final List<CartStore.Item> data = new ArrayList<>();
    private CartAdapter adapter;

    private ApiService api;
    private SessionManager session;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_cart, c, false);
        rv = v.findViewById(R.id.rvCart);
        tvTotal = v.findViewById(R.id.tvTotal);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        btnCheckout = v.findViewById(R.id.btnCheckout);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(data, new CartAdapter.OnAction() {
            @Override public void onInc(int pos) {
                data.get(pos).qty++;
                adapter.notifyItemChanged(pos);
                refreshTotal();
                syncStore();
            }
            @Override public void onDec(int pos) {
                if (data.get(pos).qty > 1) {
                    data.get(pos).qty--;
                    adapter.notifyItemChanged(pos);
                } else {
                    data.remove(pos);
                    adapter.notifyItemRemoved(pos);
                }
                refreshTotal();
                syncStore();
            }
            @Override public void onRemove(int pos) {
                data.remove(pos);
                adapter.notifyItemRemoved(pos);
                refreshTotal();
                syncStore();
            }
        });
        rv.setAdapter(adapter);

        api = ApiClient.build().create(ApiService.class);
        session = new SessionManager(requireContext());

        // nạp dữ liệu từ store
        data.clear();
        data.addAll(CartStore.all());
        adapter.notifyDataSetChanged();
        refreshTotal();

        btnCheckout.setOnClickListener(view -> doCheckout());

        return v;
    }

    private void doCheckout() {
        List<CartStore.Item> cart = CartStore.all();
        if (cart.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer uid = session.getUserId();
        if (uid == null || uid <= 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }// DÙNG getUserID() thống nhất
        if (uid == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.OrderCreateReq req = new ApiService.OrderCreateReq();
        req.user_id = new SessionManager(requireContext()).getUserId(); // KHÔNG phải getUserID()
        req.items = new ArrayList<>();
        for (CartStore.Item it : cart) {
            ApiService.OrderCreateReq.Item x = new ApiService.OrderCreateReq.Item();
            x.product_id = it.p.id;
            x.qty        = it.qty;
            x.price      = it.p.price;
            x.name       = it.p.name;   // nếu không dùng thì bỏ
            req.items.add(x);
        }

        api.createOrder(req).enqueue(new Callback<ApiService.SimpleRespId>() {
            @Override public void onResponse(Call<ApiService.SimpleRespId> c, Response<ApiService.SimpleRespId> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().ok) {
                    CartStore.clear();
                    data.clear();
                    adapter.notifyDataSetChanged();
                    refreshTotal();
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Đặt hàng thành công (#" + r.body().id + ")", Toast.LENGTH_SHORT).show();

                    // mở danh sách đơn
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.player_nav_host, new OrdersFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Tạo đơn thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ApiService.SimpleRespId> c, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshTotal() {
        long total = 0;
        for (CartStore.Item it : data) total += Math.round(it.p.price) * it.qty;
        tvTotal.setText("Tổng: " + nf.format(total) + "đ");
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void syncStore() {
        // Đồng bộ lại CartStore theo 'data'
        CartStore.clear();
        for (CartStore.Item it : data) {
            for (int i = 0; i < it.qty; i++) CartStore.add(it.p);
        }
    }
}
