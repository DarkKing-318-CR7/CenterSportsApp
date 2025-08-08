package com.example.sportcenterapp.models;

public class Order {
    private int id;
    private String courtName;
    private String timeSlot;
    private String status;

    public Order(int id, String courtName, String timeSlot, String status) {
        this.id = id;
        this.courtName = courtName;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getStatus() {
        return status;
    }
}
