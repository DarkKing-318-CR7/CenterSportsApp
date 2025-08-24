package com.example.sportcenterapp.models;

import com.google.gson.annotations.SerializedName;

public class Court {
    public int id;
    public String name, sport, surface, image;
    public int indoor;
    public double price, rating;
    @SerializedName("description") public String description;

    public Court() {}

    // Có thể thêm constructor đầy đủ nếu muốn
    public Court(int id, String name, String sport, String surface, int indoor,
                 double price, String image, String description, double rating) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.surface = surface;
        this.indoor = indoor;
        this.price = price;
        this.image = image;
        this.description = description;
        this.rating = rating;
    }

    public Court(String name, double price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }

    public int Indoor() { return indoor; }
    public void setIndoor(int indoor) { this.indoor = indoor; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    // ---- Bridge cho code cũ (nếu đâu đó còn dùng) ----
    public String getType() { return sport; }  // legacy
    public boolean isIndoor() { return indoor == 1; }


    @Override public String toString() { return name; } // để Spinner hiển thị tên
}
