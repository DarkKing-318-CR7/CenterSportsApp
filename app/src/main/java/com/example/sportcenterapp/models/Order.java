package com.example.sportcenterapp.models;

import java.util.Locale;

// Order.java
public class Order {
    public int id, userId;
    public double total;
    public String createdAt;
    public Order(int id, int userId, double total, String createdAt) {
        this.id = id; this.userId = userId; this.total = total; this.createdAt = createdAt;
    }
    public String getTotalFormatted() { return String.format(Locale.getDefault(), "%,.0fđ", total); }
}