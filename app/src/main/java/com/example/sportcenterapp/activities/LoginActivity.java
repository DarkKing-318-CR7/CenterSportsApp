package com.example.sportcenterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Spinner spinnerLoginRole;
    Button btnLogin;
    TextView tvRegister;
    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerLoginRole = findViewById(R.id.spinnerLoginRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            String selectedRole = spinnerLoginRole.getSelectedItem().toString().toLowerCase(); // user/shop/admin

            User user = db.loginUser(email, pass);
            if (user != null && user.getRole().equals(selectedRole)) {
                session.login(user);
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Chuyển hướng theo vai trò
                switch (selectedRole) {
                    case "user":
                        startActivity(new Intent(this, UserActivity.class));
                        break;
                    case "owner":
                        startActivity(new Intent(this, OwnerActivity.class));
                        break;
                    case "admin":
                        startActivity(new Intent(this, AdminActivity.class));
                        break;
                }
                finish();
            } else {
                Toast.makeText(this, "Sai tài khoản, mật khẩu hoặc vai trò", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
