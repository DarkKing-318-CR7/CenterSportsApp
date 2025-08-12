package com.example.sportcenterapp.models;

public class Court {
    public int id;
    public String name, sport, surface, image;
    public boolean indoor;
    public double pricePerHour, rating;

    public Court(int id, String name, String sport, String surface, boolean indoor,
                 double pricePerHour, double rating, String image) {
        this.id = id; this.name = name; this.sport = sport; this.surface = surface;
        this.indoor = indoor; this.pricePerHour = pricePerHour; this.rating = rating; this.image = image;
    }

    @Override public String toString() { return name; } // để Spinner hiển thị tên
}
