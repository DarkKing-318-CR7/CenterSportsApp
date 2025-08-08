package com.example.sportcenterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sportcenterapp.models.CartItem;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.models.Order;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sports_center.db";
    private static final int DATABASE_VERSION = 3;
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

        String CREATE_ORDERS_TABLE = "CREATE TABLE Orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "court_id INTEGER," +
                "time_slot TEXT," +
                "status TEXT DEFAULT 'pending'," +  // pending, approved, rejected
                "FOREIGN KEY(user_id) REFERENCES Users(id)," +
                "FOREIGN KEY(court_id) REFERENCES Courts(id))";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Trong onCreate()
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE Products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "price REAL," +
                "stock INTEGER)";
        db.execSQL(CREATE_PRODUCTS_TABLE);

        String CREATE_CART_TABLE = "CREATE TABLE Cart (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "quantity INTEGER)";
        db.execSQL(CREATE_CART_TABLE);

// Insert sản phẩm mẫu
        db.execSQL("INSERT INTO Products (name, price, stock) VALUES " +
                "('Bóng đá', 100.0, 10)," +
                "('Vợt cầu lông', 150.0, 5)," +
                "('Áo thể thao', 200.0, 20)");

        // Đơn mua (1 đơn / lần thanh toán)
        String CREATE_PURCHASES = "CREATE TABLE IF NOT EXISTS Purchases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "created_at TEXT," +
                "total REAL)";
        db.execSQL(CREATE_PURCHASES);

// Dòng hàng trong đơn
        String CREATE_PURCHASE_ITEMS = "CREATE TABLE IF NOT EXISTS PurchaseItems (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "purchase_id INTEGER," +
                "product_id INTEGER," +
                "product_name TEXT," +
                "quantity INTEGER," +
                "price_each REAL)";
        db.execSQL(CREATE_PURCHASE_ITEMS);

        String CREATE_ORDERS = "CREATE TABLE IF NOT EXISTS Orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "court_id INTEGER," +
                "time_slot TEXT," +
                "status TEXT DEFAULT 'pending')";   // pending | approved | rejected
        db.execSQL(CREATE_ORDERS);




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
    public List<Order> getOrdersByUser(int userId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT o.id, c.name, o.time_slot, o.status " +
                "FROM Orders o JOIN Courts c ON o.court_id = c.id " +
                "WHERE o.user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Order(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public void addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        values.put("quantity", quantity);
        db.insert("Cart", null, values);}

    public List<CartItem> getCartByUserId(int userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT Cart.id, product_id, quantity, name, price, stock " +
                        "FROM Cart JOIN Products ON Cart.product_id = Products.id " +
                        "WHERE user_id = ?",
                new String[]{ String.valueOf(userId) }
        );

        try {
            if (c.moveToFirst()) {
                do {
                    int cartId      = c.getInt(0);
                    int productId   = c.getInt(1);
                    int qty         = c.getInt(2);
                    String name     = c.getString(3);
                    // double price = c.getDouble(4); // nếu cần
                    // int stock   = c.getInt(5);     // nếu cần

                    // ✅ Dùng đúng constructor 5 tham số của CartItem
                    CartItem item = new CartItem(cartId, userId, productId, name, qty);
                    list.add(item);
                } while (c.moveToNext());
            }
        } finally {
            c.close(); // ✅ đóng cursor để khỏi warning leak
        }
        return list;
    }


    public void deleteCartItem(int cartItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Cart", "id = ?", new String[]{String.valueOf(cartItemId)});
        db.close();
    }
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, price, stock FROM Products", null);
        if (c.moveToFirst()) {
            do {
                list.add(new Product(
                        c.getInt(0),
                        c.getString(1),
                        c.getDouble(2), // hoặc getInt(2) nếu price là int
                        c.getInt(3)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean placeOrder(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Lấy giỏ hiện tại + tính tổng
            Cursor c = db.rawQuery(
                    "SELECT Cart.product_id, Products.name, Cart.quantity, Products.price " +
                            "FROM Cart JOIN Products ON Cart.product_id = Products.id " +
                            "WHERE Cart.user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            if (!c.moveToFirst()) { c.close(); db.endTransaction(); return false; }

            double total = 0;
            List<Object[]> rows = new ArrayList<>();
            do {
                int pid = c.getInt(0);
                String name = c.getString(1);
                int qty = c.getInt(2);
                double price = c.getDouble(3);
                total += qty * price;
                rows.add(new Object[]{pid, name, qty, price});
            } while (c.moveToNext());
            c.close();

            // Tạo record Purchases
            ContentValues p = new ContentValues();
            p.put("user_id", userId);
            p.put("created_at", String.valueOf(System.currentTimeMillis()));
            p.put("total", total);
            long purchaseId = db.insert("Purchases", null, p);
            if (purchaseId == -1) { db.endTransaction(); return false; }

            // Chèn các dòng PurchaseItems
            for (Object[] r : rows) {
                ContentValues it = new ContentValues();
                it.put("purchase_id", purchaseId);
                it.put("product_id", (Integer) r[0]);
                it.put("product_name", (String) r[1]);
                it.put("quantity", (Integer) r[2]);
                it.put("price_each", (Double) r[3]);
                db.insert("PurchaseItems", null, it);

                // Giảm tồn kho (nếu muốn)
                db.execSQL("UPDATE Products SET stock = stock - ? WHERE id = ?",
                        new Object[]{(Integer) r[2], (Integer) r[0]});
            }

            // Xoá giỏ
            db.delete("Cart", "user_id = ?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }
    // Lấy toàn bộ đơn đặt sân (join để hiển thị tên sân)
    public List<Order> getAllCourtOrders() {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT o.id, c.name, o.time_slot, o.status " +
                        "FROM Orders o JOIN Courts c ON o.court_id = c.id " +
                        "ORDER BY o.id DESC", null);

        try {
            if (c.moveToFirst()) {
                do {
                    // Order(int id, String courtName, String timeSlot, String status)
                    list.add(new Order(
                            c.getInt(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3)
                    ));
                } while (c.moveToNext());
            }
        } finally { c.close(); }
        return list;
    }

    public boolean approveOrder(int orderId) {
        ContentValues v = new ContentValues();
        v.put("status", "approved");
        return getWritableDatabase()
                .update("Orders", v, "id=?", new String[]{String.valueOf(orderId)}) > 0;
    }

    public boolean rejectOrder(int orderId) {
        ContentValues v = new ContentValues();
        v.put("status", "rejected");
        return getWritableDatabase()
                .update("Orders", v, "id=?", new String[]{String.valueOf(orderId)}) > 0;
    }

    // Tổng doanh thu (từ Purchase)
    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT IFNULL(SUM(total),0) FROM Purchases", null);
        double v = 0;
        if (c.moveToFirst()) v = c.getDouble(0);
        c.close();
        return v;
    }

    // Doanh thu hôm nay (millis -> lọc theo ngày)
    public double getTodayRevenue() {
        long start = atStartOfDay(System.currentTimeMillis());
        long end   = start + 24L*60*60*1000;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(total),0) FROM Purchases WHERE CAST(created_at AS INTEGER) BETWEEN ? AND ?",
                new String[]{ String.valueOf(start), String.valueOf(end) });
        double v = 0;
        if (c.moveToFirst()) v = c.getDouble(0);
        c.close();
        return v;
    }

    // Số đơn đặt sân đã duyệt
    public int getApprovedCourtOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Orders WHERE status='approved'", null);
        int v = 0; if (c.moveToFirst()) v = c.getInt(0); c.close(); return v;
    }

    // Số đơn đang chờ duyệt
    public int getPendingCourtOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Orders WHERE status='pending'", null);
        int v = 0; if (c.moveToFirst()) v = c.getInt(0); c.close(); return v;
    }

    // Top 5 sản phẩm bán chạy (dựa trên PurchaseItems)
    public List<String> getTopProducts() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT product_name, SUM(quantity) AS qty " +
                        "FROM PurchaseItems GROUP BY product_id, product_name " +
                        "ORDER BY qty DESC LIMIT 5", null);
        if (c.moveToFirst()) {
            do { list.add(c.getString(0) + " (" + c.getInt(1) + ")"); } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // helper: đầu ngày (millis)
    private long atStartOfDay(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    // Lấy user theo email + password khi login
    public User getUserByEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User result = null;

        Cursor c = db.rawQuery(
                "SELECT id, name, email, role FROM Users WHERE email = ? AND password = ? LIMIT 1",
                new String[]{ email, password }
        );

        if (c.moveToFirst()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String mail = c.getString(2);
            String role = c.getString(3);
            result = new User(id, name, mail, role);
        }
        c.close();
        return result;
    }




}
