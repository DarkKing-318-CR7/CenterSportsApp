package com.example.sportcenterapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    // Tên file SharedPreferences
    private static final String PREF_NAME   = "centerbooking_session";

    // Các key lưu trữ
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_USERNAME  = "username";
    private static final String KEY_ROLE      = "role";      // "player" | "admin"
    private static final String KEY_VIP       = "vip";       // boolean

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static SessionManager I;

    public SessionManager(Context ctx) {
        prefs  = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Lưu session sau khi login
    public void login(int id, String username, String role, boolean vip) {
        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role == null ? "player" : role);
        editor.putBoolean(KEY_VIP, vip);
        editor.apply();
    }

    // Setters đơn lẻ
    public void setUserId(int id)            { editor.putInt(KEY_USER_ID, id).apply(); }
    public void setUsername(String username) { editor.putString(KEY_USERNAME, username).apply(); }
    public void setRole(String role)         { editor.putString(KEY_ROLE, role).apply(); }
    public void setVip(boolean vip)          { editor.putBoolean(KEY_VIP, vip).apply(); }
    public static SessionManager get(Context c) { if (I==null) I = new SessionManager(c); return I; }

    // Getters
    public Integer getUserId() {
        return prefs.contains(KEY_USER_ID) ? prefs.getInt(KEY_USER_ID, -1) : null;
    }
    public String getUsername()  { return prefs.getString(KEY_USERNAME, null); }
    public String getRole()      { return prefs.getString(KEY_ROLE, "player"); }
    public boolean isVip()       { return prefs.getBoolean(KEY_VIP, false); }
    public boolean isLoggedIn()  { return getUserId() != null && getUserId() > 0; }

    // Đăng xuất / clear session
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
