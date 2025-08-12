package com.example.sportcenterapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.admin.AdminActivity;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.player.PlayerActivity;
import com.example.sportcenterapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            route(session.getRole());
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> doLogin());
        // demo: auto điền nhanh khi bấm vào text user
        findViewById(R.id.tvHint).setOnClickListener(v -> {
            etUser.setText("admin"); etPass.setText("admin123");
        });
        findViewById(R.id.tvHint2).setOnClickListener(v -> {
            etUser.setText("player"); etPass.setText("player123");
        });
    }

    private void doLogin() {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString().trim();
        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Nhập tài khoản & mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = db.login(u, p);
        if (user == null) {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        session.save(user.id, user.role);
        route(user.role);
        finish();
    }

    private void route(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, AdminActivity.class));
        } else {
            startActivity(new Intent(this, PlayerActivity.class));
        }
    }
}
