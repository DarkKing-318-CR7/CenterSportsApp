package com.example.sportcenterapp.player.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.utils.SessionManager;
import com.example.sportcenterapp.LoginActivity;

public class AccountFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername, tvVip, tvRole, tvCreatedAt;
    private EditText etFullName, etPhone, etEmail;
    private Button btnSave, btnChangePass, btnLogout;

    private DatabaseHelper db;
    private SessionManager session;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_account, parent, false);

        // Bind views
        ivAvatar     = v.findViewById(R.id.ivAvatar);
        tvUsername   = v.findViewById(R.id.tvUsername);
        tvVip        = v.findViewById(R.id.tvVip);
        tvCreatedAt  = v.findViewById(R.id.tvCreatedAt);
        etFullName   = v.findViewById(R.id.etFullName);
        etPhone      = v.findViewById(R.id.etPhone);
        etEmail      = v.findViewById(R.id.etEmail);
        btnSave      = v.findViewById(R.id.btnSave);
        btnChangePass= v.findViewById(R.id.btnChangePass);
        btnLogout    = v.findViewById(R.id.btnLogout); // LƯU Ý: dùng đúng id này trong layout

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        loadProfile();

        btnSave.setOnClickListener(v1 -> saveProfile());
        btnChangePass.setOnClickListener(v12 -> showChangePassDialog());
        btnLogout.setOnClickListener(v13 -> doLogout());

        return v;
    }

    private void loadProfile() {
        user = db.getUserById(session.getUserId());
        if (user == null) {
            toast("Không tải được tài khoản");
            return;
        }
        tvUsername.setText(user.username != null ? user.username : "-");
        tvVip.setText(user.vip ? "VIP" : "Thành viên thường");
        tvCreatedAt.setText(user.createdAt != null ? user.createdAt : "-");

        etFullName.setText(user.fullName != null ? user.fullName : "");
        etPhone.setText(user.phone != null ? user.phone : "");
        etEmail.setText(user.email != null ? user.email : "");

        if (user.avatar != null && !user.avatar.isEmpty()) {
            int resId = getResources().getIdentifier(user.avatar, "drawable", requireContext().getPackageName());
            if (resId != 0) ivAvatar.setImageResource(resId);
        }
    }

    private void saveProfile() {
        if (user == null) return;

        String fullName = etFullName.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();

        if (fullName.isEmpty()) { etFullName.setError("Nhập họ tên"); etFullName.requestFocus(); return; }
        if (!phone.matches("^[0-9+ ]{8,15}$")) { etPhone.setError("SĐT không hợp lệ"); etPhone.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email không hợp lệ"); etEmail.requestFocus(); return; }

        boolean ok = db.updateUserProfile(user.id, fullName, phone, email, null);
        toast(ok ? "Đã lưu" : "Lưu thất bại");
        if (ok) loadProfile();
    }

    private void showChangePassDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null, false);
        final EditText etOld = content.findViewById(R.id.etOld);
        final EditText etNew = content.findViewById(R.id.etNew);
        final EditText etRe  = content.findViewById(R.id.etRe);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(content)
                .setPositiveButton("Đổi", (d, which) -> {
                    String oldP = etOld.getText().toString();
                    String newP = etNew.getText().toString();
                    String reP  = etRe.getText().toString();

                    if (newP.length() < 4) { toast("Mật khẩu tối thiểu 4 ký tự"); return; }
                    if (!newP.equals(reP)) { toast("Xác nhận mật khẩu không khớp"); return; }

                    boolean ok = db.changePassword(user.id, oldP, newP);
                    toast(ok ? "Đổi mật khẩu thành công" : "Mật khẩu cũ không đúng");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doLogout() {
        session.clear(); // bạn đã có hàm này trong SessionManager
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void toast(String s) { Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show(); }
}
