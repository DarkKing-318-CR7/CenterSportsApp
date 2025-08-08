package com.example.sportcenterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sports_center.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_USERS = "Users";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // --> Trong DatabaseHelper.java
    public List<Court> getAllCourts() {
        List<Court> courts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Courts", null);

        if (cursor.moveToFirst()) {
            do {
                courts.add(new Court(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return courts;


    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "email TEXT UNIQUE,"
                + "password TEXT,"
                + "role TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Insert dữ liệu mẫu
        db.execSQL("INSERT INTO Users (name, email, password, role) VALUES " +
                "('Admin Root', 'admin@sport.com', 'admin123', 'admin')," +
                "('Nguyễn Văn A', 'a@gmail.com', '123456', 'player')," +
                "('Trần Thị B', 'b@gmail.com', 'abc123', 'coach')");



        String CREATE_COURTS_TABLE = "CREATE TABLE Courts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "sport TEXT, " +
                "status TEXT)";
        db.execSQL(CREATE_COURTS_TABLE);

// Thêm dữ liệu mẫu
        db.execSQL("INSERT INTO Courts (name, sport, status) VALUES " +
                "('Sân A1', 'Bóng đá', 'available')," +
                "('Sân B1', 'Cầu lông', 'maintenance')," +
                "('Sân C1', 'Pickleball', 'available')," +
                "('Sân D1', 'Bóng chuyền', 'available')");

        String CREATE_BOOKINGS_TABLE = "CREATE TABLE Bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +  // dùng fixed ID = 1 nếu chưa có đăng nhập thật
                "court_id INTEGER," +
                "time_slot TEXT," +
                "status TEXT)";
        db.execSQL(CREATE_BOOKINGS_TABLE);


    }
    public boolean bookCourt(int userId, int courtId, String timeSlot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("court_id", courtId);
        values.put("time_slot", timeSlot);
        values.put("status", "pending");

        long result = db.insert("Bookings", null, values);
        return result != -1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"id", "name", "email", "role"},
                "email=? AND password=?",
                new String[]{email, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            cursor.close();
            return user;
        }
        return null;
    }


    public void updateCourtStatus(int courtId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        db.update("Courts", values, "id = ?", new String[]{String.valueOf(courtId)});
    }

}
