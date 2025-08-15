package com.example.sportcenterapp.models;

public class Court {
    public int id;
    public String name, sport, surface, image;
    public int indoor;
    public double price, rating;

    public Court(int id, String name, String sport, String surface, int indoor,
                 double price, double rating, String image) {
        this.id = id; this.name = name; this.sport = sport; this.surface = surface;
        this.indoor = indoor; this.price = price; this.rating = rating; this.image = image;
    }

    public Court(String name, double price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }


    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    @Override public String toString() { return name; } // để Spinner hiển thị tên
}
