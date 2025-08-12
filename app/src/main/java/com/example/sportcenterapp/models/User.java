package com.example.sportcenterapp.models;


public class User {
    public int id;
    public String username;
    public String passwordHash; // demo
    public String role;         // ADMIN / PLAYER
    public String fullName;
    public String phone;
    public String vipUntil;

    public User(int id, String username, String passwordHash, String role, String fullName, String phone, String vipUntil) {
        this.id = id; this.username = username; this.passwordHash = passwordHash;
        this.role = role; this.fullName = fullName; this.phone = phone; this.vipUntil = vipUntil;
    }
}

