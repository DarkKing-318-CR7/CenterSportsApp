package com.example.sportcenterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.User;
import com.example.sportcenterapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister); // nếu không dùng có thể bỏ

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Nếu đã đăng nhập rồi thì vào thẳng Main
        if (session.getUser() != null) {
            goToMainAndClear(session.getUser());
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // Nếu bạn có RegisterActivity thì mở, không thì comment dòng này
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            });
        }
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        User u = db.getUserByEmailPassword(email, pass);
        if (u == null) {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu session + chuyển màn
        session.saveUser(u);
        goToMainAndClear(u);
    }

    private void goToMainAndClear(User u) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("role", u.getRole());
        i.putExtra("userId", u.getId());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        // finish() không cần thiết vì đã CLEAR_TASK, nhưng thêm cũng không sao
        finish();
    }
}
