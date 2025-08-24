// admin/fragments/AdminProductsFragment.java
package com.example.sportcenterapp.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.adapters.AdminProductAdapter;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.net.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment {

    private RecyclerView rv;
    private View btnAdd, empty;
    private AdminProductAdapter adapter;
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // dùng fragment_admin_products.xml bạn đã cung cấp
        return inflater.inflate(R.layout.fragment_admin_products, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        rv = v.findViewById(R.id.rvProducts);
        btnAdd = v.findViewById(R.id.btnAddProduct);
        empty = v.findViewById(R.id.emptyProducts);
        api = ApiClient.getInstance().create(ApiService.class);

        adapter = new AdminProductAdapter(requireContext(), (p, pos) -> showEditDialog(p, pos));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Vuốt trái/phải để xoá
        ItemTouchHelper.SimpleCallback swipe = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getBindingAdapterPosition();
                Product p = adapter.getItem(pos);
                confirmDelete(p, pos);
            }
        };
        new ItemTouchHelper(swipe).attachToRecyclerView(rv);

        btnAdd.setOnClickListener(v1 -> showAddDialog());

        loadProducts();
    }

    private void loadProducts() {
        // Lấy danh sách ACTIVE, trang 1, limit 50
        api.adminProducts(null, null, "ACTIVE", 1, 50, "created_at", "desc")
                .enqueue(new Callback<ApiService.ProductListResponse>() {
                    @Override public void onResponse(Call<ApiService.ProductListResponse> call, Response<ApiService.ProductListResponse> res) {
                        if (res.isSuccessful() && res.body()!=null) {
                            List<Product> items = res.body().items;
                            adapter.submitList(items);
                            empty.setVisibility(items==null || items.isEmpty()? View.VISIBLE: View.GONE);
                        } else { toast("Không tải được sản phẩm"); }
                    }
                    @Override public void onFailure(Call<ApiService.ProductListResponse> call, Throwable t) { toast("Lỗi mạng: "+t.getMessage()); }
                });
    }

    private void showAddDialog() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etStock = form.findViewById(R.id.etStock);
        EditText etImage = form.findViewById(R.id.etImage);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm sản phẩm")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String image = etImage.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) { toast("Nhập tên"); return; }

                    ApiService.ProductCreateRequest body = new ApiService.ProductCreateRequest();
                    body.name = name;
                    body.price = TextUtils.isEmpty(priceStr)? 0 : Double.parseDouble(priceStr);
                    body.stock = TextUtils.isEmpty(stockStr)? 0 : Integer.parseInt(stockStr);
                    body.image_url = image;
                    body.status = "ACTIVE"; body.category = ""; body.description = "";

                    api.createProduct(body).enqueue(new Callback<ApiService.BaseResponse>() {
                        @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                            if (res.isSuccessful() && res.body()!=null && res.body().success) {
                                toast("Đã thêm"); loadProducts();
                            } else toast("Thêm thất bại");
                        }
                        @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) { toast("Lỗi mạng: "+t.getMessage()); }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showEditDialog(Product p, int position) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etStock = form.findViewById(R.id.etStock);
        EditText etImage = form.findViewById(R.id.etImage);

        etName.setText(p.name);
        etPrice.setText(String.valueOf(p.price));
        etStock.setText(String.valueOf(p.stock));
        etImage.setText(p.image);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa sản phẩm")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    ApiService.ProductUpdateRequest body = new ApiService.ProductUpdateRequest();
                    body.name = etName.getText().toString().trim();
                    body.price = Double.parseDouble(etPrice.getText().toString().trim());
                    body.stock = Integer.parseInt(etStock.getText().toString().trim());
                    body.image_url = etImage.getText().toString().trim();

                    api.updateProduct(p.id, body).enqueue(new Callback<ApiService.BaseResponse>() {
                        @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                            if (res.isSuccessful() && res.body()!=null && res.body().success) {
                                toast("Đã lưu"); loadProducts();
                            } else toast("Lưu thất bại");
                        }
                        @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) { toast("Lỗi mạng: "+t.getMessage()); }
                    });
                })
                .setNeutralButton("Ẩn/Hiện", (d, w) -> {
                    ApiService.ProductUpdateRequest body = new ApiService.ProductUpdateRequest();
                    body.status = "ACTIVE".equalsIgnoreCase(p.status) ? "HIDDEN" : "ACTIVE";
                    api.updateProduct(p.id, body).enqueue(new Callback<ApiService.BaseResponse>() {
                        @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                            if (res.isSuccessful() && res.body()!=null && res.body().success) { toast("Đã cập nhật trạng thái"); loadProducts(); }
                            else toast("Cập nhật trạng thái thất bại");
                        }
                        @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) { toast("Lỗi mạng: "+t.getMessage()); }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void confirmDelete(Product p, int position) {
        new AlertDialog.Builder(requireContext())
                .setMessage("Xoá sản phẩm \""+p.name+"\"?")
                .setPositiveButton("Xoá", (d, w) -> {
                    api.deleteProduct(p.id).enqueue(new Callback<ApiService.BaseResponse>() {
                        @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                            if (res.isSuccessful() && res.body()!=null && res.body().success) {
                                adapter.removeAt(position);
                                if (adapter.getItemCount()==0) empty.setVisibility(View.VISIBLE);
                                toast("Đã xoá");
                            } else {
                                toast("Xoá thất bại");
                                adapter.restoreAt(p, position);
                            }
                        }
                        @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) {
                            toast("Lỗi mạng: "+t.getMessage());
                            adapter.restoreAt(p, position);
                        }
                    });
                })
                .setNegativeButton("Huỷ", (d,w) -> { adapter.restoreAt(p, position); })
                .setOnCancelListener(di -> adapter.restoreAt(p, position))
                .show();
    }

    private void toast(String m){ Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show(); }
}
