package com.example.sportcenterapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.activities.LoginActivity;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.utils.SessionManager;

public class AccountFragment extends Fragment {

    private TextView tvWelcome, tvEmail, tvRole;
    private Button btnLogout;
    private SessionManager session;

    // Nếu bạn đang gọi newInstance(userId, role) thì vẫn giữ, còn không có thể bỏ method này.
    public static AccountFragment newInstance(int userId, String role) {
        AccountFragment f = new AccountFragment();
        Bundle b = new Bundle();
        b.putInt("userId", userId);
        b.putString("userRole", role);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvEmail   = view.findViewById(R.id.tvEmail);
        tvRole    = view.findViewById(R.id.tvRole);
        btnLogout = view.findViewById(R.id.btnLogout);

        session = new SessionManager(requireContext());  // requireContext() an toàn hơn

        User user = session.getUser();
        if (user != null) {
            // Dùng string resources với placeholder thay vì nối chuỗi
            tvWelcome.setText(getString(R.string.acc_hello, user.getName()));
            tvEmail.setText(getString(R.string.acc_email, user.getEmail()));
            tvRole.setText(getString(R.string.acc_role,  user.getRole()));
        }

        btnLogout.setOnClickListener(v -> {
            // SessionManager của bạn có phương thức clear(), không phải logout()
            session.clear();

            // Quay về Login + clear backstack
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish(); // safe
        });

        return view;
    }
}
