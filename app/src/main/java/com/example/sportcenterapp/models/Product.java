package com.example.sportcenterapp.models;

public class Product {
    public int id;
    public String name;
    public double price;
    public String image; // tên file ảnh trong drawable

    public Product(int id, String name, double price, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    @Override
    public String toString() {
        return name;
    }
}
