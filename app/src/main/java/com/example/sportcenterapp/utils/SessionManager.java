package com.example.sportcenterapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "centerbooking_session";
    private static final String KEY_ID = "user_id";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(int userId, String role) {
        sp.edit().putInt(KEY_ID, userId).putString(KEY_ROLE, role).apply();
    }

    public int getUserId() { return sp.getInt(KEY_ID, -1); }
    public String getRole() { return sp.getString(KEY_ROLE, null); }

    public boolean isLoggedIn() { return getUserId() > 0 && getRole() != null; }

    public void clear() { sp.edit().clear().apply(); }
}
