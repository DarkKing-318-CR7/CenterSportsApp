package com.example.sportcenterapp.models;

import com.google.gson.annotations.SerializedName;

public class Coach {
    private int id;
    private String name;
    private String sport;
    private String level;

    // MySQL column: rate_per_hour  -> map sang field camelCase
    @SerializedName("rate_per_hour")
    private double ratePerHour;

    private String avatar;   // tên drawable hoặc tên file/URL
    private String bio;
    private String phone;
    private String email;
    private String zalo;

    // --- getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSport() { return sport; }
    public String getLevel() { return level; }
    public double getRatePerHour() { return ratePerHour; }
    public String getAvatar() { return avatar; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getZalo() { return zalo; }

    // --- setters (nếu cần dùng chỗ khác) ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSport(String sport) { this.sport = sport; }
    public void setLevel(String level) { this.level = level; }
    public void setRatePerHour(double ratePerHour) { this.ratePerHour = ratePerHour; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setZalo(String zalo) { this.zalo = zalo; }
}
