package com.example.sportcenterapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sportcenterapp.models.User;

public class SessionManager {

    private static final String PREF = "sport_center_session";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // Lưu user sau khi login
    public void saveUser(User u) {
        sp.edit()
                .putInt(KEY_ID, u.getId())
                .putString(KEY_NAME, u.getName())
                .putString(KEY_EMAIL, u.getEmail())
                .putString(KEY_ROLE, u.getRole())
                .apply();
    }

    // Lấy user hiện tại (null nếu chưa đăng nhập)
    public User getUser() {
        if (!sp.contains(KEY_ID)) return null;
        int id = sp.getInt(KEY_ID, -1);
        String name = sp.getString(KEY_NAME, null);
        String email = sp.getString(KEY_EMAIL, null);
        String role = sp.getString(KEY_ROLE, "player");
        if (id == -1 || email == null) return null;
        return new User(id, name, email, role);
    }

    // Đăng xuất
    public void clear() {
        sp.edit().clear().apply();
    }
}
