package com.example.sportcenterapp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String username;
    public String email;
    public String role;
    public boolean vip;

    @SerializedName("full_name") public String fullName;
    @SerializedName("phone")     public String phone;
    @SerializedName("address")   public String address;
    @SerializedName("avatar_url") public String avatar;
    @SerializedName("created_at") public String createdAt;
    @SerializedName("updated_at") public String updatedAt;
}
