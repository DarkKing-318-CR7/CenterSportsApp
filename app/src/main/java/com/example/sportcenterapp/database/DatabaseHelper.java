// DatabaseHelper.java
package com.example.sportcenterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.models.Order;
import com.example.sportcenterapp.models.OrderItem;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "centerbooking.db";
    // Tăng version để áp dụng schema mới (orders/order_items)
    private static final int DB_VERSION = 4;

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ==== Users ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password_hash TEXT," +
                "role TEXT," +
                "full_name TEXT," +
                "phone TEXT," +
                "vip_until TEXT," +
                "created_at TEXT" +
                ")");

        // Seed demo users
        db.execSQL("INSERT OR IGNORE INTO Users(username,password_hash,role,full_name,phone,created_at) VALUES " +
                "('admin','admin123','ADMIN','Quản trị','0909000111',date('now'))," +
                "('player','player123','PLAYER','Người chơi','0909000222',date('now'))");

        // ==== Courts ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Courts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "sport TEXT," +
                "surface TEXT," +
                "indoor INTEGER," +              // 1: indoor, 0: outdoor
                "price_per_hour REAL," +
                "rating REAL," +
                "image TEXT" +
                ")");

        // Seed courts
        db.execSQL("INSERT INTO Courts(name,sport,surface,indoor,price_per_hour,rating,image) VALUES " +
                "('Sân 7 người','Bóng đá','Cỏ nhân tạo',0,250000,4.3,'court_soccer')," +
                "('Sân BC 01','Bóng chuyền','PU',1,150000,4.6,'court_volleyball')," +
                "('Cầu lông 01','Cầu lông','Gỗ',1,120000,4.8,'court_badminton')");

        // ==== Bookings ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "court_id INTEGER," +
                "date TEXT," +        // yyyy-MM-dd
                "start_time TEXT," +  // HH:mm
                "end_time TEXT," +    // HH:mm
                "status TEXT," +      // CONFIRMED/CANCELLED
                "total_price REAL" +
                ")");

        // ==== Products ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "price REAL," +       // dùng REAL để nhất quán
                "image TEXT" +
                ")");

        // Seed products
        db.execSQL("INSERT INTO Products(name,price,image) VALUES " +
                "('Nước suối Aquafina', 8000, 'product_water')," +
                "('Bóng đá Size 5', 150000, 'product_soccer_ball')," +
                "('Áo thể thao Nike', 250000, 'product_tshirt')," +
                "('Vợt cầu lông Yonex', 350000, 'product_badminton_racket')");

        // ==== CartItems ====
        db.execSQL("CREATE TABLE IF NOT EXISTS CartItems (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "qty INTEGER" +
                ")");

        // ==== Orders (schema CHUẨN – chữ thường) ====
        db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "total REAL NOT NULL," +
                "created_at TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +      // giữ REAL cho đồng nhất với Products.price
                "quantity INTEGER NOT NULL" +
                ")");

        // ⚠️ Không tạo thêm Orders/OrderItems chữ HOA để tránh nhầm lẫn schema
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Đảm bảo bảng mới tồn tại (không drop dữ liệu cũ trừ khi bạn muốn)
        if (oldV < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "total REAL NOT NULL," +
                    "created_at TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_id INTEGER NOT NULL," +
                    "product_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "quantity INTEGER NOT NULL" +
                    ")");
        }
    }

    // ===== Users API =====
    public User login(String username, String password) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT id, role, full_name, phone, vip_until " +
                             "FROM Users WHERE username=? AND password_hash=?",
                     new String[]{username, password})) {
            if (c.moveToFirst()) {
                return new User(
                        c.getInt(0), username, null,
                        c.getString(1),  // role
                        c.getString(2),  // full_name
                        c.getString(3),  // phone
                        c.getString(4)   // vip_until
                );
            }
        }
        return null;
    }

    // ===== Courts API =====
    public List<Court> getCourts() {
        List<Court> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT id,name,sport,surface,indoor,price_per_hour,rating,image " +
                             "FROM Courts ORDER BY name", null)) {
            while (c.moveToNext()) {
                list.add(new Court(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4) == 1,
                        c.getDouble(5),
                        c.getDouble(6),
                        c.getString(7)
                ));
            }
        }
        return list;
    }

    // ===== Booking API =====
    public boolean hasConflict(int courtId, String date, String start, String end) {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE court_id=? AND date=? " +
                "AND NOT( end_time<=? OR start_time>=? ) AND status='CONFIRMED'";
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(sql, new String[]{
                     String.valueOf(courtId), date, start, end})) {
            if (c.moveToFirst()) return c.getInt(0) > 0;
        }
        return false;
    }

    public long createBooking(int userId, int courtId, String date, String start, String end, double total) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("court_id", courtId);
        v.put("date", date);
        v.put("start_time", start);
        v.put("end_time", end);
        v.put("status", "CONFIRMED");
        v.put("total_price", total);
        try (SQLiteDatabase db = getWritableDatabase()) {
            return db.insert("Bookings", null, v);
        }
    }

    // ===== Shop / Cart API =====
    public List<Product> getProducts() {
        List<Product> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT id, name, price, image FROM Products", null)) {
            while (c.moveToNext()) {
                list.add(new Product(
                        c.getInt(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getString(3)
                ));
            }
        }
        return list;
    }

    public void addToCart(int userId, int productId, int qty) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.execSQL("UPDATE CartItems SET qty = qty + ? WHERE user_id=? AND product_id=?",
                    new Object[]{qty, userId, productId});
            try (Cursor c = db.rawQuery("SELECT changes()", null)) {
                if (c.moveToFirst() && c.getInt(0) == 0) {
                    ContentValues v = new ContentValues();
                    v.put("user_id", userId);
                    v.put("product_id", productId);
                    v.put("qty", qty);
                    db.insert("CartItems", null, v);
                }
            }
        }
    }

    public int getCartCount(int userId) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT IFNULL(SUM(qty),0) FROM CartItems WHERE user_id=?",
                     new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public List<Object[]> getCartItems(int userId) {
        List<Object[]> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT ci.product_id, p.name, p.price, ci.qty, p.image " +
                             "FROM CartItems ci JOIN Products p ON ci.product_id=p.id " +
                             "WHERE ci.user_id=?",
                     new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                list.add(new Object[]{
                        c.getInt(0),       // product_id
                        c.getString(1),    // name
                        c.getDouble(2),    // price
                        c.getInt(3),       // qty
                        c.getString(4)     // image
                });
            }
        }
        return list;
    }

    public void updateCartQty(int userId, int productId, int qty) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            if (qty <= 0) {
                db.delete("CartItems", "user_id=? AND product_id=?",
                        new String[]{String.valueOf(userId), String.valueOf(productId)});
            } else {
                ContentValues v = new ContentValues();
                v.put("qty", qty);
                db.update("CartItems", v, "user_id=? AND product_id=?",
                        new String[]{String.valueOf(userId), String.valueOf(productId)});
            }
        }
    }

    /** Tổng tiền giỏ (double cho nhất quán với Products.price REAL) */
    public double getCartTotal(int userId) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT IFNULL(SUM(p.price * ci.qty), 0) " +
                             "FROM CartItems ci JOIN Products p ON ci.product_id = p.id " +
                             "WHERE ci.user_id = ?",
                     new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0d;
    }

    // ===== Orders API =====

    /**
     * Tạo đơn hàng từ giỏ hiện tại của user:
     * - Ghi vào orders (user_id, total, created_at)
     * - Ghi vào order_items (order_id, product_id, name, price, quantity)
     * - Xoá CartItems của user
     * @return orderId (>0 nếu OK), -1 nếu thất bại/giỏ trống
     */
    public long checkoutFromCart(int userId) {
        SQLiteDatabase dbw = getWritableDatabase();
        Cursor c = null;
        boolean started = false;

        try {
            // Bắt đầu transaction sớm để tránh endTransaction khi chưa begin
            dbw.beginTransaction();
            started = true;

            // Tính tổng
            double total = 0d;
            c = dbw.rawQuery(
                    "SELECT IFNULL(SUM(p.price * ci.qty), 0) " +
                            "FROM CartItems ci JOIN Products p ON ci.product_id = p.id " +
                            "WHERE ci.user_id = ?",
                    new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) total = c.getDouble(0);
            c.close(); c = null;

            if (total <= 0d) {
                return -1; // rollback ở finally vì chưa setSuccessful
            }

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            // Insert orders
            ContentValues ov = new ContentValues();
            ov.put("user_id", userId);
            ov.put("total", total);
            ov.put("created_at", now);
            long orderId = dbw.insertOrThrow("orders", null, ov);

            // Insert order_items từ CartItems
            c = dbw.rawQuery(
                    "SELECT ci.product_id, p.name, p.price, ci.qty " +
                            "FROM CartItems ci JOIN Products p ON ci.product_id = p.id " +
                            "WHERE ci.user_id = ?",
                    new String[]{String.valueOf(userId)});
            while (c.moveToNext()) {
                ContentValues iv = new ContentValues();
                iv.put("order_id", orderId);
                iv.put("product_id", c.getInt(0));
                iv.put("name", c.getString(1));
                iv.put("price", c.getDouble(2));   // REAL
                iv.put("quantity", c.getInt(3));   // map từ qty -> quantity
                dbw.insertOrThrow("order_items", null, iv);
            }

            // Xoá giỏ của user
            dbw.delete("CartItems", "user_id=?", new String[]{String.valueOf(userId)});

            dbw.setTransactionSuccessful();
            return orderId;

        } catch (Exception e) {
            Log.e("DB", "checkout error", e);
            return -1;
        } finally {
            if (c != null) c.close();
            if (started && dbw.inTransaction()) dbw.endTransaction();
        }
    }

    public List<Order> getOrders(int userId) {
        List<Order> out = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT id, total, created_at FROM orders WHERE user_id=? ORDER BY id DESC",
                     new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                // Order.total là REAL -> nếu model Order của bạn dùng int, hãy đổi sang double
                out.add(new Order(c.getInt(0), userId, c.getDouble(1), c.getString(2)));
            }
        }
        return out;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> out = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT product_id, name, price, quantity FROM order_items WHERE order_id=?",
                     new String[]{String.valueOf(orderId)})) {
            while (c.moveToNext()) {
                out.add(new OrderItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getDouble(2),  // REAL
                        c.getInt(3)
                ));
            }
        }
        return out;
    }
}
