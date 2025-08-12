package com.example.sportcenterapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportcenterapp.LoginActivity;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.utils.SessionManager;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin);

        Button btnLogout = findViewById(R.id.btnLogoutAdmin);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
