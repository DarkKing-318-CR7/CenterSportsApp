package com.example.sportcenterapp;// imports cần có
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.admin.AdminActivity;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.player.PlayerActivity; // sửa theo package của bạn
// import com.example.sportcenterapp.admin.AdminActivity; // nếu có

public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;
    private Button btnLogin;
    private DatabaseHelper db;
    private com.example.sportcenterapp.utils.SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);

        db = new DatabaseHelper(this);
        session = new com.example.sportcenterapp.utils.SessionManager(this);

        btnLogin.setOnClickListener(v -> doLogin());

        TextView tvDemoAdmin = findViewById(R.id.tvHint);
        TextView tvDemoPlayer = findViewById(R.id.tvHint2);

        EditText etUser = findViewById(R.id.etUser);
        EditText etPass = findViewById(R.id.etPass);

// Khi bấm vào demo admin
        tvDemoAdmin.setOnClickListener(v -> {
            etUser.setText("admin");
            etPass.setText("admin123");
        });

// Khi bấm vào demo player
        tvDemoPlayer.setOnClickListener(v -> {
            etUser.setText("player");
            etPass.setText("player123");
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


        // Lưu session ĐÚNG API
        session.login(user.id, user.username, user.role, user.vip);

        // Điều hướng theo role (nếu chưa có AdminActivity, luôn đi PlayerActivity)
        route(user.role);
        finish();
    }

    private void route(String role) {
        Intent i;
        if ("admin".equalsIgnoreCase(role)) {
            // i = new Intent(this, AdminActivity.class); // nếu bạn có màn admin
            i = new Intent(this, AdminActivity.class);   // tạm thời
        } else {
            i = new Intent(this, PlayerActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}

