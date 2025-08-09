package com.example.sportcenterapp.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Court {
    private int id;
    private String name;
    private String sport;
    private String status;

    public Court(int id, String name, String sport, String status) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSport() { return sport; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

}

