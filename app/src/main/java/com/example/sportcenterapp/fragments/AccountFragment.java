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

    TextView tvWelcome, tvEmail, tvRole;
    Button btnLogout;
    SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        btnLogout = view.findViewById(R.id.btnLogout);
        session = new SessionManager(getContext());

        User user = session.getUser();

        tvWelcome.setText("Xin chào, " + user.getName());
        tvEmail.setText("Email: " + user.getEmail());
        tvRole.setText("Vai trò: " + user.getRole());

        btnLogout.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }
}
