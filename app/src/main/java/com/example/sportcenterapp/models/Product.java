package com.example.sportcenterapp.models;

public class Product {
    int id;
    String name;
    double price;
    int stock;

    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getter...
    public String getName() {
        return name;
    }

    public double getPrice(){
        return price;
    }

    public int getId() { return id; }

    // nếu chưa có:


}
