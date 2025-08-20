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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class AccountFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername, tvVip, tvCreatedAt;
    private EditText etFullName, etPhone, etEmail,etAddress;
    private View groupDetails;
    private Button btnViewDetails, btnEdit, btnSave, btnCancel,
            btnChangePass, btnBookingHistory, btnOrderHistory, btnChat, btnLogout;

    private User user;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        // Header
        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvUsername = v.findViewById(R.id.tvUsername);
        tvVip = v.findViewById(R.id.tvVip);

        // Details (ẩn mặc định)
        groupDetails = v.findViewById(R.id.groupDetails);
        tvCreatedAt = v.findViewById(R.id.tvCreatedAt);
        etFullName  = v.findViewById(R.id.etFullName);
        etPhone     = v.findViewById(R.id.etPhone);
        etEmail     = v.findViewById(R.id.etEmail);
        etAddress =v.findViewById(R.id.etAddress);

        btnViewDetails   = v.findViewById(R.id.btnViewDetails);
        btnEdit          = v.findViewById(R.id.btnEdit);
        btnSave          = v.findViewById(R.id.btnSave);
        btnCancel        = v.findViewById(R.id.btnCancel);
        btnChangePass    = v.findViewById(R.id.btnChangePass);
        btnBookingHistory= v.findViewById(R.id.btnBookingHistory);
        btnOrderHistory  = v.findViewById(R.id.btnOrderHistory);
        btnChat          = v.findViewById(R.id.btnChat);
        btnLogout        = v.findViewById(R.id.btnLogout);

        loadUser();
        setupActions();

        return v;
    }

    private void loadUser() {
        int uid = new SessionManager(requireContext()).getUserId();
        try (DatabaseHelper db = new DatabaseHelper(requireContext())) {
            user = db.getUserById(uid);
        }
        if (user == null) return;

        // Header
        tvUsername.setText(user.username + " • " + safe(user.fullName));
        // Hiển thị thông tin VIP
        tvVip.setText(user.vip ? "VIP" : "Thành viên thường");


        // Avatar: nếu bạn lưu tên drawable trong cột avatar (vd: "ic_person")
        int resId = getResources().getIdentifier(
                user.avatar != null ? user.avatar : "ic_person",
                "drawable",
                requireContext().getPackageName()
        );
        ivAvatar.setImageResource(resId != 0 ? resId : R.drawable.ic_person);

        // Details (khi mở)
        etFullName.setText(safe(user.fullName));
        etPhone.setText(safe(user.phone));
        etEmail.setText(safe(user.email));
        etAddress.setText(safe(user.address));
        tvCreatedAt.setText(safe(user.createdAt));

        setEditable(false);
    }

    private void setupActions() {
        btnViewDetails.setOnClickListener(v -> {
            groupDetails.setVisibility(groupDetails.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            btnViewDetails.setText(groupDetails.getVisibility()==View.VISIBLE
                    ? "Ẩn thông tin" : "Xem thông tin");
        });

        btnEdit.setOnClickListener(v -> setEditable(true));

        btnCancel.setOnClickListener(v -> {
            // khôi phục dữ liệu cũ
            etFullName.setText(safe(user.fullName));
            etPhone.setText(safe(user.phone));
            etEmail.setText(safe(user.email));
            etAddress.setText(safe(user.address));
            setEditable(false);
        });

        btnSave.setOnClickListener(v -> {
            String full = etFullName.getText().toString().trim();
            String phone= etPhone.getText().toString().trim();
            String email= etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            try (DatabaseHelper db = new DatabaseHelper(requireContext())) {
                db.updateUserProfile(user.id, full, phone, email, address,null);
            }
            Snackbar.make(requireView(), "Đã lưu thay đổi", Snackbar.LENGTH_SHORT).show();
            // cập nhật lại header + state
            user.fullName = full; user.phone = phone; user.email = email;user.address= address;
            tvUsername.setText(user.username + " • " + safe(user.fullName));
            setEditable(false);
        });

        btnChangePass.setOnClickListener(v -> showChangePasswordDialog());

        btnBookingHistory.setOnClickListener(v ->
                navigateTo(new BookingHistoryFragment()));

        btnOrderHistory.setOnClickListener(v ->
                navigateTo(new OrdersFragment()));

        btnChat.setOnClickListener(v ->
                navigateTo(new ChatSupportFragment()));

        btnLogout.setOnClickListener(v -> {
            new SessionManager(requireContext()).logout();
            requireActivity().finish(); // hoặc chuyển về LoginActivity
        });
    }

    private void setEditable(boolean editable){
        etFullName.setEnabled(editable);
        etPhone.setEnabled(editable);
        etEmail.setEnabled(editable);
        etAddress.setEnabled(editable);

        btnEdit.setVisibility(editable ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void showChangePasswordDialog() {
        // dialog đơn giản
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null, false);
        EditText etOld = content.findViewById(R.id.etOld);
        EditText etNew = content.findViewById(R.id.etNew);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(content)
                .setPositiveButton("Đổi", (d, w) -> {
                    String oldP = etOld.getText().toString();
                    String newP = etNew.getText().toString();
                    try (DatabaseHelper db = new DatabaseHelper(requireContext())) {
                        // TODO: bạn cài đặt db.updatePassword(user.id, oldP, newP)
                        // db.updatePassword(user.id, oldP, newP);
                    }
                    Snackbar.make(requireView(), "Đổi mật khẩu thành công", Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateTo(Fragment f){
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.player_nav_host, f) // id container của PlayerActivity
                .addToBackStack(null)
                .commit();
    }

    private static String safe(String s){ return s==null ? "" : s; }
}
