package com.example.sportcenterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Spinner spinnerRole;
    Button btnRegister;
    TextView tvLogin;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        db = new DatabaseHelper(this);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = db.registerUser(name, email, password, role);
            if (success) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại LoginActivity
            } else {
                Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
