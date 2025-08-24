package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.ProductAdapter;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.CartStore;

import java.util.*;
import retrofit2.*;

public class ShopFragment extends Fragment implements ProductAdapter.OnAction {

    private RecyclerView rv;
    private final List<Product> data = new ArrayList<>();
    private ProductAdapter adapter;
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_shop, c, false);
        rv = v.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(data, this);
        rv.setAdapter(adapter);

        api = ApiClient.get().create(ApiService.class);
        loadProducts();
        return v;
    }

    private void loadProducts(){
        api.getProducts(null).enqueue(new Callback<List<Product>>() {
            @Override public void onResponse(Call<List<Product>> call, Response<List<Product>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body()!=null){
                    data.clear(); data.addAll(r.body()); adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(),"Không tải được sản phẩm",Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Product>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ProductAdapter.OnAction
    @Override public void onAddToCart(Product p) {
        CartStore.add(p);
        Toast.makeText(getContext(),"Đã thêm vào giỏ ("+CartStore.totalCount()+")", Toast.LENGTH_SHORT).show();
        // TODO: nếu có badge số lượng giỏ, gọi hàm update badge ở Activity
    }

    @Override public void onClick(Product p) {
        // TODO: mở màn chi tiết (optional)
    }
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        MenuHost host = (MenuHost) requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_shop_top, menu);   // có action_cart
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_cart) {
                    openCart();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void openCart() {
        // Nếu bạn có BottomNav tab “Giỏ”, có thể gọi Activity để switch tab.
        // Còn không, replace fragment như dưới:
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.player_nav_host, new CartFragment())
                .addToBackStack(null)
                .commit();
    }
}
