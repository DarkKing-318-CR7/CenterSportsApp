package com.example.sportcenterapp.models;

public class Product {
    public int id;
    public String name;
    public double price;
    public String image; // tên file ảnh trong drawable
    public int stock;
    public Product(int id, String name, double price, String image,int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.stock=stock;
    }

    public Product() {}

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
    public int getStock(){
        return stock;
    }

    public String getImage() {
        return image;
    }

    public int getId() {
        return id;
    }
}
