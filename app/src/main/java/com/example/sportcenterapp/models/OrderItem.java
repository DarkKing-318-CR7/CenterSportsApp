package com.example.sportcenterapp.models;

import java.util.Locale;

public class OrderItem {
    public int productId, qty;
    public String name;
    public double price;
    public OrderItem(int productId, String name, double price, int qty) {
        this.productId = productId; this.name = name; this.price = price; this.qty = qty;
    }
    public String getLineTotalFormatted() { return String.format(Locale.getDefault(), "%,.0fđ", price * qty); }
}