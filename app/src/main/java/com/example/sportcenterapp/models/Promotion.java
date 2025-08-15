package com.example.sportcenterapp.models;

public class Promotion {
    private String title;
    private int imageResId;

    public Promotion(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}
