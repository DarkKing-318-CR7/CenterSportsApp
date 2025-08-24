package com.example.sportcenterapp.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.adapters.AdminProductAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductsFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rv;
    private TextView empty;
    private AdminProductAdapter adapter;
    private final List<Product> data = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_products, container, false);

        db = new DatabaseHelper(requireContext());
        rv = root.findViewById(R.id.rvProducts);
        empty = root.findViewById(R.id.emptyProducts);
        Button btnAdd = root.findViewById(R.id.btnAddProduct);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminProductAdapter(data, new AdminProductAdapter.OnAction() {
            @Override public void onEdit(Product p) { showEditProductDialog(p); }
            @Override public void onDelete(Product p) { confirmDeleteProduct(p); } // sẽ gọi khi swipe
        });
        rv.setAdapter(adapter);

        // Vuốt trái/phải để xoá (confirm; hủy -> khôi phục)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos < 0 || pos >= data.size()) return;
                Product p = data.get(pos);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xoá sản phẩm")
                        .setMessage("Xoá \"" + (p.getName()!=null?p.getName():("SP #"+p.getId())) + "\"?")
                        .setPositiveButton("Xoá", (d, w) -> {
                            db.deleteProduct(p.getId());  // nếu đã có API thì gọi API ở đây
                            loadData();
                        })
                        .setNegativeButton("Huỷ", (d, w) -> adapter.notifyItemChanged(pos))
                        .show();
            }
        }).attachToRecyclerView(rv);

        btnAdd.setOnClickListener(v -> showAddProductDialog());
        loadData();
        return root;
    }

    private void loadData() {
        data.clear();
        data.addAll(db.getAllProducts());
        adapter.notifyDataSetChanged();
        if (empty != null) empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ===== CREATE =====
    private void showAddProductDialog() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null, false);
        EditText etName  = form.findViewById(R.id.etName);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etStock = form.findViewById(R.id.etStock);
        EditText etImage = form.findViewById(R.id.etImage);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm sản phẩm")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String img  = etImage.getText().toString().trim();
                    int stock   = safeInt(etStock.getText().toString(), 0);
                    double price= safeDouble(etPrice.getText().toString(), -1);
                    if (TextUtils.isEmpty(name) || price <= 0) { toast("Tên/Giá không hợp lệ"); return; }
                    db.createProduct(name, price, stock, img);
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ===== UPDATE =====
    private void showEditProductDialog(Product p) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null, false);
        EditText etName  = form.findViewById(R.id.etName);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etStock = form.findViewById(R.id.etStock);
        EditText etImage = form.findViewById(R.id.etImage);

        etName.setText(p.getName());
        etPrice.setText(String.format(Locale.getDefault(), "%.0f", p.getPrice()));
        etStock.setText(String.valueOf(p.getStock()));
        etImage.setText(p.getImage());

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa sản phẩm")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String img  = etImage.getText().toString().trim();
                    int stock   = safeInt(etStock.getText().toString(), 0);
                    double price= safeDouble(etPrice.getText().toString(), -1);
                    if (TextUtils.isEmpty(name) || price <= 0) { toast("Tên/Giá không hợp lệ"); return; }
                    db.updateProduct(p.getId(), name, price, stock, img);
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteProduct(Product p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn chắc chắn muốn xóa \"" + p.getName() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> { db.deleteProduct(p.getId()); loadData(); })
                .setNegativeButton("Hủy", (d, w) -> adapter.notifyDataSetChanged())
                .show();
    }

    private int safeInt(String s, int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
    private double safeDouble(String s, double def){ try{ return Double.parseDouble(s.trim()); } catch(Exception e){ return def; } }
    private void toast(String m){ Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show(); }
}
