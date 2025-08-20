package com.example.sportcenterapp.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CourtAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Court;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourtsFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rv;
    private TextView empty;
    private CourtAdapter adapter;
    private final List<Court> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_courts, container, false);

        db = new DatabaseHelper(requireContext());

        rv = root.findViewById(R.id.rvCourts);
        empty = root.findViewById(R.id.emptyCourts);
        Button btnAdd = root.findViewById(R.id.btnAddCourt);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CourtAdapter(
                items,
                new CourtAdapter.OnCourtAction() {
                    @Override public void onEdit(Court c) { showEditCourtDialog(c); }
                    @Override public void onDelete(Court c) { confirmDeleteCourt(c); }
                    @Override public void onClick(Court c) { /* không dùng ở admin */ }
                },
                true // Admin mode: tap = Edit
        );
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddCourtDialog());

        loadData();
        return root;
    }

    private void loadData() {
        items.clear();
        items.addAll(db.getAllCourts());
        adapter.notifyDataSetChanged();
        if (empty != null) empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /* -------------------- CREATE -------------------- */
    private void showAddCourtDialog() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_court, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etSport = form.findViewById(R.id.etSport);
        EditText etSurface = form.findViewById(R.id.etSurface);
        CheckBox cbIndoor = form.findViewById(R.id.cbIndoor);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etImage = form.findViewById(R.id.etImage);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm sân")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String sport = etSport.getText().toString().trim();
                    String surface = etSurface.getText().toString().trim();
                    boolean indoor = cbIndoor.isChecked();
                    double price = safeDouble(etPrice.getText().toString(), -1);
                    String image = etImage.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(surface) || price <= 0) {
                        toast("Nhập đủ Tên/Sport/Surface và Giá > 0");
                        return;
                    }
                    db.createCourt(name, sport, surface, indoor, price, image); // boolean indoor
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /* -------------------- UPDATE -------------------- */
    private void showEditCourtDialog(Court c) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_court, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etSport = form.findViewById(R.id.etSport);
        EditText etSurface = form.findViewById(R.id.etSurface);
        CheckBox cbIndoor = form.findViewById(R.id.cbIndoor);
        EditText etPrice = form.findViewById(R.id.etPrice);
        EditText etImage = form.findViewById(R.id.etImage);

        etName.setText(c.getName());
        etSport.setText(c.getSport());
        etSurface.setText(c.getSurface());
        cbIndoor.setChecked(c.Indoor() == 1); // model lưu 0/1
        etPrice.setText(String.format(Locale.getDefault(),"%.0f", c.getPrice()));
        etImage.setText(c.getImage());

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa sân")
                .setView(form)
                .setPositiveButton("Cập nhật", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String sport = etSport.getText().toString().trim();
                    String surface = etSurface.getText().toString().trim();
                    boolean indoor = cbIndoor.isChecked();
                    double price = safeDouble(etPrice.getText().toString(), -1);
                    String image = etImage.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(surface) || price <= 0) {
                        toast("Nhập đủ Tên/Sport/Surface và Giá > 0");
                        return;
                    }
                    db.updateCourt(c.getId(), name, sport, surface, indoor, price, image); // boolean indoor
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /* -------------------- DELETE -------------------- */
    private void confirmDeleteCourt(Court c) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sân")
                .setMessage("Xóa sân \"" + c.getName() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deleteCourt(c.getId());
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private double safeDouble(String s, double def) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; } }
    private void toast(String m) { Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show(); }
}
