package com.example.sportcenterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
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
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();

            User user = db.loginUser(email, pass);
            if (user != null) {
                session.login(user);
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
