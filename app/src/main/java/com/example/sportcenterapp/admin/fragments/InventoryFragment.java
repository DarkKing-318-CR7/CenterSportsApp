package com.example.sportcenterapp.admin.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryFragment extends Fragment {

    private static final int MODE_PRODUCTS = 0;
    private static final int MODE_COURTS   = 1;

    private int mode = MODE_COURTS; // mặc định
    private RecyclerView rv;
    private MaterialButton btnAdd;
    private DatabaseHelper db;

    private final List<Object> data = new ArrayList<>();
    private ListAdapter adapter;

    // === PICK IMAGE (OpenDocument -> lưu URI) ===
    private EditText currentImageTarget;
    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    // cố gắng giữ quyền đọc lâu dài
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    if (currentImageTarget != null) currentImageTarget.setText(uri.toString());
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        rv = v.findViewById(R.id.rvList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListAdapter(data, new ListAdapter.Listener() {
            @Override public void onClick(int position) { /* mở chi tiết nếu cần */ }
            @Override public void onLongClick(int position) {
                Object item = data.get(position);
                if (item instanceof Court) showCourtOptions((Court) item);
                else if (item instanceof Product) showProductOptions((Product) item);
            }
        });
        rv.setAdapter(adapter);

        btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> {
            if (mode == MODE_COURTS) showAddCourtDialog(null);
            else showAddProductDialog(null);
        });

        MaterialButtonToggleGroup tg = v.findViewById(R.id.invToggle);
        tg.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            mode = (checkedId == R.id.btnCourts) ? MODE_COURTS : MODE_PRODUCTS;
            load();
            btnAdd.setText(mode == MODE_COURTS ? "Thêm sân" : "Thêm sản phẩm");
        });
        tg.check(R.id.btnCourts);
    }

    private void load() {
        data.clear();
        if (mode == MODE_COURTS) data.addAll(db.getAllCourts());
        else                     data.addAll(db.getAllProducts());
        adapter.notifyDataSetChanged();
    }

    /* ====================== PRODUCT ====================== */

    private void showProductOptions(Product p) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(p.name)
                .setItems(new String[]{"Sửa", "Xoá"}, (d, which) -> {
                    if (which == 0) showAddProductDialog(p);
                    else confirmDeleteProduct(p);
                }).show();
    }

    private void confirmDeleteProduct(Product p) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xoá sản phẩm")
                .setMessage("Bạn chắc muốn xoá \"" + p.name + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xoá", (dd, w) -> {
                    boolean ok = db.deleteProduct(p.id);
                    Toast.makeText(getContext(), ok ? "Đã xoá" : "Lỗi xoá", Toast.LENGTH_SHORT).show();
                    load();
                }).show();
    }

    private void showAddProductDialog(@Nullable Product editing) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_product, null, false);
        EditText edName  = form.findViewById(R.id.edProdName);
        EditText edPrice = form.findViewById(R.id.edProdPrice);
        EditText edStock = form.findViewById(R.id.edProdStock);
        EditText edImage = form.findViewById(R.id.edProdImage);
        Button   btnPick = form.findViewById(R.id.btnPickProdImage);

        if (editing != null) {
            edName.setText(editing.name);
            edPrice.setText(String.valueOf(editing.price));
            edStock.setText(String.valueOf(editing.stock));
            edImage.setText(editing.image != null ? editing.image : "");
        }

        btnPick.setOnClickListener(v -> {
            currentImageTarget = edImage;
            pickImage.launch(new String[]{"image/*"});
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editing == null ? "Thêm sản phẩm" : "Sửa sản phẩm")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", (d, w) -> {
                    String name  = edName.getText().toString().trim();
                    String sPrice= edPrice.getText().toString().trim();
                    String sStock= edStock.getText().toString().trim();
                    String image = edImage.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sPrice) || TextUtils.isEmpty(sStock)) {
                        Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double price = safeDouble(sPrice);
                    int stock    = safeInt(sStock);

                    boolean ok;
                    if (editing == null) {
                        ok = db.createProduct(name, price, stock, image.isEmpty()? null : image) > 0;
                    } else {
                        ok = db.updateProduct(editing.id, name, price, stock, image.isEmpty()? null : image);
                    }
                    Toast.makeText(getContext(), ok ? "Đã lưu" : "Lỗi lưu", Toast.LENGTH_SHORT).show();
                    load();
                }).show();
    }

    /* ======================== COURT ======================== */

    private void showCourtOptions(Court c) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(c.name)
                .setItems(new String[]{"Sửa", "Xoá"}, (d, which) -> {
                    if (which == 0) showAddCourtDialog(c);
                    else confirmDeleteCourt(c);
                }).show();
    }

    private void confirmDeleteCourt(Court c) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xoá sân")
                .setMessage("Bạn chắc muốn xoá \"" + c.name + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xoá", (dd, w) -> {
                    boolean ok = db.deleteCourt(c.id);
                    Toast.makeText(getContext(), ok ? "Đã xoá" : "Lỗi xoá", Toast.LENGTH_SHORT).show();
                    load();
                }).show();
    }

    private void showAddCourtDialog(@Nullable Court editing) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_court, null, false);
        EditText edName    = form.findViewById(R.id.edCourtName);
        EditText edSport   = form.findViewById(R.id.edCourtSport);
        EditText edSurface = form.findViewById(R.id.edCourtSurface);
        EditText edPrice   = form.findViewById(R.id.edCourtPrice);
        EditText edImage   = form.findViewById(R.id.edCourtImage);
        Switch   swIndoor  = form.findViewById(R.id.swCourtIndoor);
        Button   btnPick   = form.findViewById(R.id.btnPickCourtImage);

        if (editing != null) {
            edName.setText(editing.name);
            edSport.setText(editing.sport);
            edSurface.setText(editing.surface);
            edPrice.setText(String.valueOf(editing.price));
            edImage.setText(editing.image != null ? editing.image : "");
            swIndoor.setChecked(editing.indoor == 1);
        }

        btnPick.setOnClickListener(v -> {
            currentImageTarget = edImage;
            pickImage.launch(new String[]{"image/*"});
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editing == null ? "Thêm sân" : "Sửa sân")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(editing == null ? "Thêm" : "Lưu", (d, w) -> {
                    String name    = edName.getText().toString().trim();
                    String sport   = edSport.getText().toString().trim();
                    String surface = edSurface.getText().toString().trim();
                    String sPrice  = edPrice.getText().toString().trim();
                    String image   = edImage.getText().toString().trim();
                    boolean indoor = swIndoor.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(sPrice)) {
                        Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double price = safeDouble(sPrice);

                    boolean ok;
                    if (editing == null) {
                        ok = db.createCourt(name, sport, surface, indoor, price, image.isEmpty()? null : image) > 0;
                    } else {
                        ok = db.updateCourt(editing.id, name, sport, surface, indoor, price, image.isEmpty()? null : image);
                    }
                    Toast.makeText(getContext(), ok ? "Đã lưu" : "Lỗi lưu", Toast.LENGTH_SHORT).show();
                    load();
                }).show();
    }

    /* ==================== Adapter chung ==================== */

    private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.VH> {
        interface Listener {
            void onClick(int position);
            void onLongClick(int position);
        }
        private final List<Object> data;
        private final Listener listener;

        ListAdapter(List<Object> d, Listener l){ this.data = d; this.listener = l; }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle, status;
            VH(@NonNull View v){
                super(v);
                title = v.findViewById(R.id.tvTitle);
                subtitle = v.findViewById(R.id.tvSubtitle);
                status = v.findViewById(R.id.tvStatus);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_row, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Object o = data.get(pos);
            h.status.setVisibility(View.GONE); // inventory không dùng trạng thái

            if (o instanceof Court) {
                Court c = (Court) o;
                h.title.setText(c.name);
                String indoor = c.indoor == 1 ? "Trong nhà" : "Ngoài trời";
                h.subtitle.setText(c.sport + " • " + indoor + " • " + (c.surface != null ? c.surface : "")
                        + "\n" + money(c.price) + "/giờ");
            } else {
                Product p = (Product) o;
                h.title.setText(p.name);
                h.subtitle.setText(money(p.price) + "  •  Tồn: " + p.stock);
            }

            h.itemView.setOnClickListener(v -> listener.onClick(h.getBindingAdapterPosition()));
            h.itemView.setOnLongClickListener(v -> { listener.onLongClick(h.getBindingAdapterPosition()); return true; });
        }

        @Override public int getItemCount(){ return data.size(); }

        private static String money(double v) {
            return NumberFormat.getNumberInstance(new Locale("vi","VN"))
                    .format(Math.round(v)) + "đ";
        }
    }

    /* ==================== helpers ==================== */
    private static double safeDouble(String s) {
        try { return Double.parseDouble(s.replace(",", "").trim()); } catch (Exception e) { return 0; }
    }
    private static int safeInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
