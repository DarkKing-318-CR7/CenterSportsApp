package com.example.sportcenterapp.player.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.LoginActivity;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.example.sportcenterapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername, tvVip, tvCreatedAt;
    private EditText etFullName, etPhone, etEmail, etAddress;
    private View btnViewDetails, groupDetails, btnEdit, btnSave, btnCancel;
    private View btnChangePass, btnBookingHistory, btnOrderHistory, btnChat, btnLogout;

    private ApiService api;
    private int userId;
    private User current;
    private SessionManager sm;
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        // bind views — các ID khớp file fragment_account.xml
        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvUsername = v.findViewById(R.id.tvUsername);
        tvVip = v.findViewById(R.id.tvVip);
        tvCreatedAt = v.findViewById(R.id.tvCreatedAt);

        btnViewDetails = v.findViewById(R.id.btnViewDetails);
        groupDetails = v.findViewById(R.id.groupDetails);

        etFullName = v.findViewById(R.id.etFullName);
        etPhone = v.findViewById(R.id.etPhone);
        etEmail = v.findViewById(R.id.etEmail);
        etAddress = v.findViewById(R.id.etAddress);

        btnEdit = v.findViewById(R.id.btnEdit);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);

        btnChangePass = v.findViewById(R.id.btnChangePass);
        btnBookingHistory = v.findViewById(R.id.btnBookingHistory);
        btnOrderHistory = v.findViewById(R.id.btnOrderHistory);
        btnChat = v.findViewById(R.id.btnChat);
        btnLogout = v.findViewById(R.id.btnLogout);

        api = ApiClient.getInstance().create(ApiService.class);
        userId = requireContext().getSharedPreferences("session", 0).getInt("user_id", 0);

        setEditMode(false);
        etEmail.setEnabled(false); // không cho đổi email

        btnViewDetails.setOnClickListener(v1 -> toggleDetails());
        btnEdit.setOnClickListener(v12 -> setEditMode(true));
        btnCancel.setOnClickListener(v13 -> { bind(current); setEditMode(false); });
        btnSave.setOnClickListener(v14 -> saveProfile());

        btnChangePass.setOnClickListener(v15 -> showChangePasswordDialog());
        btnBookingHistory.setOnClickListener(v16 -> {
            // mở BookingHistoryFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new BookingHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnOrderHistory.setOnClickListener(v17 -> {
            // mở OrderHistoryFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new OrdersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnChat.setOnClickListener(v18 -> {
            // mở ChatSupportFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_nav_host, new ChatSupportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        View btnLogout = v.findViewById(R.id.btnLogout);
        btnLogout.setEnabled(true);            // phòng trường hợp đang bị disable trong XML
        btnLogout.setOnClickListener(view -> doLogout());
    }

    private void toggleDetails() {
        int vis = groupDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        groupDetails.setVisibility(vis);
        if (vis == View.VISIBLE && current != null) bind(current);
        if (btnViewDetails instanceof Button) {
            ((Button) btnViewDetails).setText(
                    vis == View.VISIBLE ? getString(R.string.action_hide_details) : getString(R.string.action_view_details)
            );
        }
    }

    private void setEditMode(boolean edit) {
        etFullName.setEnabled(edit);
        etPhone.setEnabled(edit);
        etAddress.setEnabled(edit);

        btnEdit.setVisibility(edit ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(edit ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(edit ? View.VISIBLE : View.GONE);
    }

    private void loadProfile() {
        sm = SessionManager.get(requireContext());
        int userId = sm.getUserId();
        if (userId <= 0) {
            toast("Chưa đăng nhập");
            // Gợi ý: chuyển về LoginActivity nếu cần
            // startActivity(new Intent(requireContext(), LoginActivity.class));
            // requireActivity().finish();
            return;
        }
        api.getUserProfile(userId).enqueue(new Callback<ApiService.UserResponse>() {
            @Override public void onResponse(Call<ApiService.UserResponse> call, Response<ApiService.UserResponse> res) {
                if (!res.isSuccessful() || res.body()==null || !res.body().success || res.body().user==null) {
                    toast("Không tải được hồ sơ"); return;
                }
                current = res.body().user;
                bind(current);
            }
            @Override public void onFailure(Call<ApiService.UserResponse> call, Throwable t) { toast("Lỗi mạng: " + t.getMessage()); }
        });
    }

    private void bind(User u) {
        if (u == null) return;
        tvUsername.setText(n(u.username));
        tvVip.setText(getString(R.string.member_regular));
        tvCreatedAt.setText(getString(R.string.label_created_at) + " " + n(u.createdAt));

        etFullName.setText(n(u.fullName));
        etPhone.setText(n(u.phone));
        etEmail.setText(n(u.email));
        etAddress.setText(n(u.address));

        String avatar = n(u.avatar);
        if (!avatar.isEmpty() && (avatar.startsWith("http") || avatar.startsWith("/"))) {
            Glide.with(this).load(avatar).placeholder(R.drawable.ic_person).into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    private void saveProfile() {
        if (current == null) return;
        ApiService.UserUpdateRequest body = new ApiService.UserUpdateRequest();
        body.fullName = etFullName.getText().toString().trim();
        body.phone = etPhone.getText().toString().trim();
        body.address = etAddress.getText().toString().trim();
        // nếu cho phép đổi avatar, bạn có thể thêm field ở UI; hiện layout của bạn không có ô avatar nên bỏ qua

        api.updateUser(userId, body).enqueue(new Callback<ApiService.BaseResponse>() {
            @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                if (res.isSuccessful() && res.body()!=null && res.body().success) {
                    toast("Đã lưu thay đổi");
                    // cập nhật local
                    current.fullName = body.fullName;
                    current.phone = body.phone;
                    current.address = body.address;
                    setEditMode(false);
                } else toast("Lưu thất bại");
            }
            @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) { toast("Lỗi mạng: " + t.getMessage()); }
        });
    }

    private void showChangePasswordDialog() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null, false);
        EditText etOld = form.findViewById(R.id.etOld);
        EditText etNew = form.findViewById(R.id.etNew);
        EditText etRe  = form.findViewById(R.id.etRe);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_change_password)
                .setView(form)
                .setPositiveButton(R.string.action_change, (d,w)->{
                    String o = etOld.getText().toString();
                    String n = etNew.getText().toString();
                    String r = etRe.getText().toString();
                    if (n.length() < 6) { toast("Mật khẩu mới phải ≥ 6 ký tự"); return; }
                    if (!n.equals(r)) { toast("Nhập lại mật khẩu chưa khớp"); return; }

                    ApiService.ChangePasswordRequest body = new ApiService.ChangePasswordRequest();
                    body.oldPassword = o; body.newPassword = n;
                    api.changePassword(userId, body).enqueue(new Callback<ApiService.BaseResponse>() {
                        @Override public void onResponse(Call<ApiService.BaseResponse> call, Response<ApiService.BaseResponse> res) {
                            if (res.isSuccessful() && res.body()!=null && res.body().success) toast("Đã đổi mật khẩu");
                            else toast("Đổi mật khẩu thất bại");
                        }
                        @Override public void onFailure(Call<ApiService.BaseResponse> call, Throwable t) { toast("Lỗi mạng: " + t.getMessage()); }
                    });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void doLogout() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.action_logout, (d, w) -> {
                    // 1. Xoá session
                    requireContext()
                            .getSharedPreferences("session", Context.MODE_PRIVATE)
                            .edit().clear().apply();

                    // 2. Thông báo
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    // 3. Điều hướng về LoginActivity, xoá toàn bộ back stack
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);

                    // 4. Kết thúc Activity hiện tại
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }


    private String n(String s){ return s==null? "" : s; }
    private void toast(String m){ Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show(); }
}
