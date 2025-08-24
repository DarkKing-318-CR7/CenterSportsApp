package com.example.sportcenterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.admin.AdminActivity;
import com.example.sportcenterapp.player.PlayerActivity;
import com.example.sportcenterapp.utils.SessionManager;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity – dùng API PHP (Retrofit) thay cho SQLite
 * YÊU CẦU: Đã tạo ApiClient + ApiService như đã hướng dẫn, và cấp INTERNET + cleartextTraffic trong Manifest.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin;
    private TextView tvDemoAdmin, tvDemoPlayer;

    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvDemoAdmin = findViewById(R.id.tvHint);
        tvDemoPlayer = findViewById(R.id.tvHint2);

        session = new SessionManager(this);
        api = ApiClient.build().create(ApiService.class);

        // Auto-fill demo (khớp dữ liệu seed trong sportcenter_seed.sql: admin/123, player/123)
        tvDemoAdmin.setOnClickListener(v -> {
            etUser.setText("admin");
            etPass.setText("123");
        });
        tvDemoPlayer.setOnClickListener(v -> {
            etUser.setText("player");
            etPass.setText("123");
        });

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Nhập tài khoản & mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        api.login(new ApiService.LoginReq(u, p)).enqueue(new Callback<ApiService.UserDTO>() {
            @Override
            public void onResponse(Call<ApiService.UserDTO> call, Response<ApiService.UserDTO> resp) {
                btnLogin.setEnabled(true);

                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService.UserDTO ud = resp.body();

                // Lưu phiên – tuỳ SessionManager của bạn, đổi tên hàm cho khớp
                // (ở project bạn trước đây: session.login(int id, String username, String role, boolean vip))
                session.login(ud.id, ud.username, ud.role, ud.vip == 1);

                // Điều hướng
                if ("admin".equalsIgnoreCase(ud.role)) {
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    startActivity(new Intent(LoginActivity.this, PlayerActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                finish();
            }

            @Override
            public void onFailure(Call<ApiService.UserDTO> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
