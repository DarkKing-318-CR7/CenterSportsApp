package com.example.sportcenterapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sportcenterapp.models.User;

public class SessionManager {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context context;

    public static final String PREF_NAME = "user_session";

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void login(User user) {
        editor.putInt("id", user.getId());
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("role", user.getRole());
        editor.apply();
    }

    public User getUser() {
        return new User(
                prefs.getInt("id", -1),
                prefs.getString("name", null),
                prefs.getString("email", null),
                prefs.getString("role", null)
        );
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains("email");
    }
}
