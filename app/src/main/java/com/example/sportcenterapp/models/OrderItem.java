package com.example.sportcenterapp.models;

import java.util.Locale;

public class OrderItem {
    public int productId, quantity;
    public String name;
    public double price;
    public OrderItem(int productId, String name, double price, int quantity) {
        this.productId = productId; this.name = name; this.price = price; this.quantity = quantity;
    }
    public String getLineTotalFormatted() { return String.format(Locale.getDefault(), "%,.0fđ", price * quantity); }
}