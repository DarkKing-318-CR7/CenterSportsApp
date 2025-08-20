package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import java.util.List;

public class UsersFragment extends Fragment {
    private RecyclerView rv;
    private View empty;
    private DatabaseHelper db;
    private UserAdapter adapter;
    private List<User> data;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup p, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_admin_users, p, false);
        rv = v.findViewById(R.id.rvUsers);
        empty = v.findViewById(R.id.emptyUsers);
        v.findViewById(R.id.btnAddUser).setOnClickListener(x -> showAddDialog(null));

        db = new DatabaseHelper(requireContext());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        load();
        return v;
    }

    private void load() {
        data = db.getAllUsers(); // cần API ở DatabaseHelper
        adapter = new UserAdapter(data, new UserAdapter.Listener() {
            @Override public void onEdit(User u) { showAddDialog(u); }
            @Override public void onDelete(User u) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Xoá tài khoản \"" + u.username + "\"?")
                        .setPositiveButton("Xoá", (d,w)->{ db.deleteUser(u.id); load(); })
                        .setNegativeButton("Huỷ", null).show();
            }
            @Override public void onToggleVip(User u) { db.setVip(u.id, !u.vip); load(); }
            @Override public void onToggleRole(User u) {
                String next = "admin".equalsIgnoreCase(u.role) ? "player" : "admin";
                db.setRole(u.id, next); load();
            }
        });
        rv.setAdapter(adapter);
        empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE);
    }

    private void showAddDialog(@Nullable User edit) {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_user, null, false);
        EditText etUsername = content.findViewById(R.id.etUsername);
        EditText etPassword = content.findViewById(R.id.etPassword);
        EditText etFull     = content.findViewById(R.id.etFullname);
        EditText etPhone    = content.findViewById(R.id.etPhone);
        EditText etEmail    = content.findViewById(R.id.etEmail);
        EditText etAddress  =content.findViewById(R.id.etAddress);
        Switch   swVip      = content.findViewById(R.id.swVip);
        Spinner  spRole     = content.findViewById(R.id.spRole);

        spRole.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new String[]{"player","admin"}));

        boolean isEdit = (edit != null);
        if (isEdit) {
            etUsername.setText(edit.username);
            etUsername.setEnabled(false);
            etFull.setText(edit.fullName);
            etPhone.setText(edit.phone);
            etEmail.setText(edit.email);
            etAddress.setText(edit.address);
            swVip.setChecked(edit.vip);
            spRole.setSelection("admin".equalsIgnoreCase(edit.role)?1:0);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit? "Sửa tài khoản" : "Thêm tài khoản")
                .setView(content)
                .setPositiveButton(isEdit? "Lưu" : "Thêm", (d,w)->{
                    String u = etUsername.getText().toString().trim();
                    String p = etPassword.getText().toString().trim();
                    String f = etFull.getText().toString().trim();
                    String ph= etPhone.getText().toString().trim();
                    String em= etEmail.getText().toString().trim();
                    String adr =etAddress.getText().toString().trim();
                    String role = (String) spRole.getSelectedItem();
                    boolean vip = swVip.isChecked();

                    if (u.isEmpty() || (!isEdit && p.isEmpty())) {
                        Toast.makeText(requireContext(), "Nhập username/mật khẩu", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEdit) {
                        db.updateUserProfile(edit.id, f, ph, em,adr, null);
                        db.setVip(edit.id, vip);
                        db.setRole(edit.id, role);
                        if (!p.isEmpty()) db.changePassword(edit.id, edit.username.equals(u)?p:p, p); // đổi pass nếu nhập
                    } else {
                        db.createUser(u, p, role, f, ph, em, vip);
                    }
                    load();
                })
                .setNegativeButton("Huỷ", null).show();
    }

    // -------- Adapter nội bộ nhanh gọn --------
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.H> {
        interface Listener {
            void onEdit(User u);
            void onDelete(User u);
            void onToggleVip(User u);
            void onToggleRole(User u);
        }
        private final List<User> data;
        private final Listener l;
        UserAdapter(List<User> d, Listener l){ this.data=d; this.l=l; }
        static class H extends RecyclerView.ViewHolder {
            TextView tvName, tvInfo, tvRoleVip;
            ImageButton btnEdit, btnDel, btnVip, btnRole;
            H(View v){ super(v);
                tvName=v.findViewById(R.id.tvName);
                tvInfo=v.findViewById(R.id.tvInfo);
                tvRoleVip=v.findViewById(R.id.tvRoleVip);
                btnEdit=v.findViewById(R.id.btnEdit);
                btnDel=v.findViewById(R.id.btnDelete);
                btnVip=v.findViewById(R.id.btnVip);
                btnRole=v.findViewById(R.id.btnRole);
            }
        }
        @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup p, int t){
            View v=LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_user,p,false);
            return new H(v);
        }
        @Override public void onBindViewHolder(@NonNull H h, int i){
            User u=data.get(i);
            h.tvName.setText(u.username + (u.fullName!=null && !u.fullName.isEmpty() ? " • " + u.fullName : ""));
            String info = (u.phone==null?"":u.phone) + (u.email==null?"":(" • "+u.email));
            h.tvInfo.setText(info.trim().isEmpty()? "-" : info);
            h.tvRoleVip.setText((u.role==null?"player":u.role) + (u.vip?" • VIP":""));
            h.btnEdit.setOnClickListener(v->l.onEdit(u));
            h.btnDel.setOnClickListener(v->l.onDelete(u));
            h.btnVip.setOnClickListener(v->l.onToggleVip(u));
            h.btnRole.setOnClickListener(v->l.onToggleRole(u));
        }
        @Override public int getItemCount(){ return data.size(); }
    }
}
