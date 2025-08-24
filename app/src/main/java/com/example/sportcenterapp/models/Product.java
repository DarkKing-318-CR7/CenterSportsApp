package com.example.sportcenterapp.models;

import com.google.gson.annotations.SerializedName;

public class Product {
    public int id;
    public String name;
    public String description;
    public double price;
    public int stock;

    @SerializedName("image_url") public String image;
    public String category;
    public String status;

    // nếu PHP dùng cột active (0/1)
    public int active; // 1 = ACTIVE

    @SerializedName("created_at") public String createdAt;
    @SerializedName("updated_at") public String updatedAt;


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
