// DatabaseHelper.java
package com.example.sportcenterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.models.ChatMessage;
import com.example.sportcenterapp.models.Coach;
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
    private static final int DB_VERSION = 5;

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ==== Users ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +                          // demo: vẫn dùng plaintext
                "full_name TEXT," +
                "phone TEXT," +
                "email TEXT," +
                "vip INTEGER NOT NULL DEFAULT 0 CHECK (vip IN (0,1))," +
                "avatar TEXT," +
                "role TEXT NOT NULL DEFAULT 'player' CHECK (role IN ('player','admin'))," +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))" +
                ")"
        );




        // Seed demo users
        db.execSQL(
                "INSERT OR IGNORE INTO Users(username,password,role,full_name,phone,created_at) VALUES " +
                        "('admin','admin123','admin','Quản trị','0909000111',date('now'))," +
                        "('player','player123','player','Người chơi','0909000222',date('now'))"
        );


        // ==== Courts ====
        db.execSQL("CREATE TABLE IF NOT EXISTS Courts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "sport TEXT," +
                "surface TEXT," +
                "indoor INTEGER," +              // 1: indoor, 0: outdoor
                "price REAL," +
                "rating REAL," +
                "image TEXT" +
                ")");

        // Seed courts
        db.execSQL("INSERT INTO Courts(name,sport,surface,indoor,price,rating,image) VALUES " +
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
                "image TEXT," +
                "stock INTEGER"+
                ")");

        // Seed products
        db.execSQL("INSERT INTO Products(name,price,image,stock) VALUES " +
                "('Bóng đá Size 5', 150000, 'product_soccer_ball', 12)," +
                "('Áo thể thao Nike', 250000, 'product_tshirt', 12)," +
                "('Vợt cầu lông Yonex', 350000, 'product_badminton_racket', 23)," +
                "('Giày chạy Adidas', 800000, 'product_running_shoes', 15)," +
                "('Găng tay thủ môn', 180000, 'product_goalkeeper_gloves', 10)," +
                "('Bóng rổ Spalding', 320000, 'product_basketball', 8)," +
                "('Dây nhảy thể lực', 50000, 'product_jump_rope', 25)," +
                "('Thảm tập yoga', 200000, 'product_yoga_mat', 20)");


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
                "status TEXT NOT NULL DEFAULT 'pending'," +   // <— CỘT NÀY
                "booking_id INTEGER," +
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

        // DatabaseHelper.onCreate(...)
        db.execSQL("CREATE TABLE IF NOT EXISTS Coaches (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "sport TEXT," +                 // bộ môn: Tennis/Badminton/Football...
                "level TEXT," +                 // Beginner/Intermediate/Pro
                "rate_per_hour REAL," +         // có thể null nếu bạn không dùng
                "avatar TEXT," +                // tên file drawable hoặc url
                "bio TEXT," +
                "phone TEXT," +                 // SĐT
                "email TEXT," +                 // Email
                "zalo TEXT)");                  // ID/phone Zalo (tùy chọn)

        // chỉ insert nếu rỗng
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Coaches", null);
        if (c.moveToFirst() && c.getInt(0) == 0) {
            db.execSQL("INSERT INTO Coaches(name,sport,level,rate_per_hour,avatar,bio,phone,email,zalo) VALUES" +
                    "('Nguyễn Minh','Tennis','Pro',350000,'coach_tennis_1','10 năm kinh nghiệm','0901002003','minh.tennis@example.com','0901002003')," +
                    "('Trần Hòa','Badminton','Intermediate',250000,'coach_badminton_1','HLV cộng đồng','0905006007','hoa.badminton@example.com','0905006007')," +
                    "('Zinédine Zidane','Football','Pro',5000000,'coach_football_1','Chuyên kỹ thuật tiền đạo','0912345678','vy.football@example.com','0912345678')");
        }
        c.close();

        db.execSQL("CREATE TABLE IF NOT EXISTS ChatMessages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sender_role TEXT NOT NULL," +           // 'player' hoặc 'admin'
                "message TEXT NOT NULL," +
                "timestamp TEXT NOT NULL" +              // yyyy-MM-dd HH:mm:ss
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS Courts(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, sport TEXT, surface TEXT, indoor INTEGER, price REAL, image TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Products(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, price REAL, stock INTEGER, image TEXT)");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        try { db.execSQL("ALTER TABLE Users ADD COLUMN created_at TEXT"); } catch (Exception ignored) {}
        try { db.execSQL("UPDATE Users SET created_at = datetime('now','localtime') " +
                "WHERE created_at IS NULL OR created_at=''"); } catch (Exception ignored) {}

        // thêm role nếu thiếu
        try { db.execSQL("ALTER TABLE Users ADD COLUMN role TEXT"); } catch (Exception ignored) {}
        try { db.execSQL("UPDATE Users SET role='player' WHERE role IS NULL OR role=''"); } catch (Exception ignored) {}

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
            db.execSQL("ALTER TABLE Bookings ADD COLUMN created_at TEXT DEFAULT (datetime('now','localtime'))");
        }
        try { db.execSQL("ALTER TABLE orders ADD COLUMN status TEXT NOT NULL DEFAULT 'pending'"); } catch (Exception ignored) {}
        try { db.execSQL("ALTER TABLE orders ADD COLUMN booking_id INTEGER"); } catch (Exception ignored) {}
        try { db.execSQL("ALTER TABLE orders ADD COLUMN created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))"); } catch (Exception ignored) {}
        if (oldV < 5) {
            try { db.execSQL("ALTER TABLE orders ADD COLUMN status TEXT DEFAULT 'pending'"); } catch (Exception ignore) {}
        }
    }

    // Dòng model phục vụ UI list Admin (nếu bạn chưa có class riêng)
    public static class OrderRow {
        public long id; public String code; public String customer;
        public double total; public String status; public String createdAt; public @Nullable String courtName;
        public OrderRow(long id, String code, String customer, double total, String status, String createdAt, @Nullable String courtName) {
            this.id=id; this.code=code; this.customer=customer; this.total=total; this.status=status; this.createdAt=createdAt; this.courtName=courtName;
        }
    }

    public List<OrderRow> getOrdersAdmin(@Nullable String statusFilter) {
        ArrayList<OrderRow> out = new ArrayList<>();
        String base =
                "SELECT o.id, IFNULL(u.full_name,u.username) AS customer, o.total, o.status, o.created_at, " +
                        "       c.name AS court_name " +
                        "FROM orders o " +
                        "JOIN Users u ON u.id=o.user_id " +
                        "LEFT JOIN Bookings b ON b.id=o.booking_id " +
                        "LEFT JOIN Courts c ON c.id=b.court_id ";
        String tail = " ORDER BY o.id DESC";
        String sql; String[] args = null;
        if (statusFilter==null || "all".equalsIgnoreCase(statusFilter)) { sql = base + tail; }
        else { sql = base + " WHERE o.status=? " + tail; args = new String[]{ statusFilter }; }

        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                out.add(new OrderRow(
                        id, "#OD-" + id, c.getString(1), c.getDouble(2),
                        c.getString(3), c.getString(4),
                        c.isNull(5) ? null : c.getString(5)
                ));
            }
        }
        return out;
    }

    public void updateOrderStatus(long orderId, String status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        getWritableDatabase().update("orders", cv, "id=?", new String[]{ String.valueOf(orderId) });
    }


    // ===== Users API =====
    // DatabaseHelper.java
    public @Nullable com.example.sportcenterapp.models.User login(String username, String password) {
        String sql = "SELECT id, username, full_name, phone, email, vip, avatar, role, created_at " +
                "FROM Users WHERE username=? AND password=? LIMIT 1";

        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(sql, new String[]{username, password})) {
            if (c.moveToFirst()) {
                com.example.sportcenterapp.models.User u = new com.example.sportcenterapp.models.User();
                u.id        = c.getInt(0);
                u.username  = c.getString(1);
                u.fullName  = c.getString(2);
                u.phone     = c.getString(3);
                u.email     = c.getString(4);
                u.vip       = (c.getInt(5) == 1);
                u.avatar    = c.getString(6);
                u.role      = c.getString(7);
                u.createdAt = c.getString(8);
                return u;
            }
        }
        return null;
    }


    public User getUserById(int userId) {
        User u = null;
        String sql = "SELECT id, username, full_name, phone, email, vip, avatar, created_at, role " +
                "FROM Users WHERE id=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                u = new User();
                u.id = c.getInt(0);
                u.username = c.getString(1);
                u.fullName = c.getString(2);
                u.phone = c.getString(3);
                u.email = c.getString(4);
                u.vip = c.getInt(5) == 1;
                u.avatar = c.getString(6);
                u.createdAt = c.getString(7);
                u.role = c.getString(8);
            }
        }
        return u;
    }

    public boolean updateUserProfile(int userId, String fullName, String phone, String email, @Nullable String avatar) {
        ContentValues v = new ContentValues();
        v.put("full_name", fullName);
        v.put("phone", phone);
        v.put("email", email);
        if (avatar != null) v.put("avatar", avatar);
        return getWritableDatabase().update("Users", v, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean changePassword(int userId, String oldPass, String newPass) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT password FROM Users WHERE id=?", new String[]{String.valueOf(userId)})) {
            if (!c.moveToFirst() || !c.getString(0).equals(oldPass)) return false;
        }
        ContentValues v = new ContentValues();
        v.put("password", newPass);
        return getWritableDatabase().update("Users", v, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }



    // ===== Courts API =====
    public List<Court> getCourts() {
        List<Court> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT id,name,sport,surface,indoor,price,rating,image " +
                             "FROM Courts ORDER BY name", null)) {
            while (c.moveToNext()) {
                list.add(new Court(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4),
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

    // Lấy giá theo sân
    public double getCourtRate(int courtId) {
        try (Cursor c = getReadableDatabase()
                .rawQuery("SELECT price_per_hour FROM Courts WHERE id=?",
                        new String[]{String.valueOf(courtId)})) {
            return (c.moveToFirst() ? c.getDouble(0) : 0);
        }
    }

    public long createBooking(int userId, int courtId, String date, String start, String end, double total) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("court_id", courtId);
        v.put("date", date);
        v.put("start_time", start);
        v.put("end_time", end);
        v.put("status", "PENDING");
        v.put("total_price", total);
        try (SQLiteDatabase db = getWritableDatabase()) {
            return db.insert("Bookings", null, v);
        }
    }
    public int updateBookingStatus(long bookingId, String newStatus) {
        ContentValues v = new ContentValues();
        v.put("status", newStatus); // "PENDING" | "CONFIRMED" | "CANCELLED" | "COMPLETED"
        return getWritableDatabase().update("Bookings", v, "id=?",
                new String[]{String.valueOf(bookingId)});
    }

    public List<com.example.sportcenterapp.models.Booking> getAllBookings() {
        String sql = "SELECT b.id, b.date, b.start_time, b.end_time, b.status, b.total_price," +
                "       c.name, c.image " +
                "FROM Bookings b JOIN Courts c ON b.court_id=c.id " +
                "ORDER BY b.date DESC, b.start_time DESC, b.id DESC";
        List<com.example.sportcenterapp.models.Booking> list = new java.util.ArrayList<>();
        try (Cursor cur = getReadableDatabase().rawQuery(sql, null)) {
            while (cur.moveToNext()) {
                com.example.sportcenterapp.models.Booking m = new com.example.sportcenterapp.models.Booking();
                m.id = cur.getInt(0);
                m.date = cur.getString(1);
                m.startTime = cur.getString(2);
                m.endTime = cur.getString(3);
                m.status = cur.getString(4);
                m.totalPrice = cur.getDouble(5);
                m.courtName = cur.getString(6);
                m.courtImage = cur.getString(7);
                list.add(m);
            }
        }
        return list;
    }



    // models/Booking.java gợi ý: id, courtName, date, startTime, endTime, status, totalPrice, createdAt, image
    public List<com.example.sportcenterapp.models.Booking> getBookingsByUser(int userId) {
        String sql =
                "SELECT b.id, b.date, b.start_time, b.end_time, b.status, b.total_price, " +
                        "       c.name, c.image " +
                        "FROM Bookings b JOIN Courts c ON b.court_id = c.id " +
                        "WHERE b.user_id=? " +
                        "ORDER BY b.date DESC, b.start_time DESC, b.id DESC";

        List<com.example.sportcenterapp.models.Booking> list = new java.util.ArrayList<>();
        try (android.database.Cursor cur = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)})) {
            while (cur.moveToNext()) {
                com.example.sportcenterapp.models.Booking m = new com.example.sportcenterapp.models.Booking();
                m.id = cur.getInt(0);
                m.date = cur.getString(1);
                m.startTime = cur.getString(2);
                m.endTime = cur.getString(3);
                m.status = cur.getString(4);
                m.totalPrice = cur.getDouble(5);
                m.courtName = cur.getString(6);
                m.courtImage = cur.getString(7);
                // m.createdAt = null; // nếu model có field này thì để trống
                list.add(m);
            }
        }
        return list;
    }



    // ===== Shop / Cart API =====
    public List<Product> getProducts() {
        List<Product> list = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor c = db.rawQuery("SELECT id, name, price, image,stock FROM Products", null)) {
            while (c.moveToNext()) {
                list.add(new Product(
                        c.getInt(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getString(3),
                        c.getInt(4)
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

    public List<String> getCoachSports() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cur = db.rawQuery("SELECT DISTINCT sport FROM Coaches WHERE sport IS NOT NULL AND sport<>'' ORDER BY sport", null)) {
            while (cur.moveToNext()) list.add(cur.getString(0));
        }
        return list;
    }

    public List<Coach> getCoachesBySport(@Nullable String sport) {
        List<Coach> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur;
        if (sport == null || sport.equals("Tất cả")) {
            cur = db.rawQuery("SELECT id,name,sport,level,rate_per_hour,avatar,bio,phone,email,zalo FROM Coaches ORDER BY name", null);
        } else {
            cur = db.rawQuery("SELECT id,name,sport,level,rate_per_hour,avatar,bio,phone,email,zalo FROM Coaches WHERE sport=? ORDER BY name", new String[]{sport});
        }
        try (cur) {
            while (cur.moveToNext()) {
                Coach m = new Coach();
                m.id = cur.getInt(0);
                m.name = cur.getString(1);
                m.sport = cur.getString(2);
                m.level = cur.getString(3);
                m.ratePerHour = cur.getDouble(4);
                m.avatar = cur.getString(5);
                m.bio = cur.getString(6);
                m.phone = cur.getString(7);
                m.email = cur.getString(8);
                m.zalo = cur.getString(9);
                list.add(m);
            }
        }
        return list;
    }

    //chatbox
    public void addChatMessage(String senderRole, String message) {
        ContentValues v = new ContentValues();
        v.put("sender_role", senderRole);
        v.put("message", message);
        v.put("timestamp", new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        getWritableDatabase().insert("ChatMessages", null, v);
    }

    public List<ChatMessage> getAllChatMessages() {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT id, sender_role, message, timestamp FROM ChatMessages ORDER BY id ASC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                ChatMessage m = new ChatMessage();
                m.id = c.getInt(0);
                m.senderRole = c.getString(1);
                m.message = c.getString(2);
                m.timestamp = c.getString(3);
                list.add(m);
            }
        }
        return list;
    }

    /** Admin: cập nhật trạng thái đơn hàng: "pending" | "approved" | "cancelled" | "fulfilled"(nếu dùng) */
    public boolean updateStatus(long orderId, String newStatus) {
        ContentValues v = new ContentValues();
        v.put("status", newStatus);
        return getWritableDatabase().update("orders", v, "id=?", new String[]{String.valueOf(orderId)}) > 0;
    }



    //admin
    // Lấy tất cả user (trừ khi bạn muốn ẩn tài khoản hệ thống)
    public java.util.List<com.example.sportcenterapp.models.User> getAllUsers() {
        var list = new java.util.ArrayList<com.example.sportcenterapp.models.User>();
        String sql = "SELECT id, username, full_name, phone, email, vip, avatar, created_at, role FROM Users ORDER BY username";
        try (android.database.Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                var u = new com.example.sportcenterapp.models.User();
                u.id = c.getInt(0); u.username = c.getString(1); u.fullName = c.getString(2);
                u.phone = c.getString(3); u.email = c.getString(4); u.vip = c.getInt(5)==1;
                u.avatar = c.getString(6); u.createdAt = c.getString(7); u.role = c.getString(8);
                list.add(u);
            }
        }
        return list;
    }

    // Tạo user mới
    public boolean createUser(String username, String password, String role, String full, String phone, String email, boolean vip) {
        android.content.ContentValues v = new android.content.ContentValues();
        v.put("username", username);
        v.put("password", password);   // demo plaintext
        v.put("role", role);
        v.put("full_name", full);
        v.put("phone", phone);
        v.put("email", email);
        v.put("vip", vip ? 1 : 0);
        v.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date()));
        return getWritableDatabase().insert("Users", null, v) > 0;
    }

    // Xoá user
    public boolean deleteUser(int id) {
        return getWritableDatabase().delete("Users", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Set VIP
    public boolean setVip(int id, boolean isVip) {
        android.content.ContentValues v = new android.content.ContentValues();
        v.put("vip", isVip ? 1 : 0);
        return getWritableDatabase().update("Users", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Set Role
    public boolean setRole(int id, String role) {
        android.content.ContentValues v = new android.content.ContentValues();
        v.put("role", role);
        return getWritableDatabase().update("Users", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    //admin
    // ===== COURTS (ADMIN) =====

    // Create
    public long createCourt(String name, String sport, String surface, boolean indoor, double price, @Nullable String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("sport", sport);
        v.put("surface", surface);
        v.put("indoor", indoor ? 1 : 0);   // map boolean -> 0/1
        v.put("price", price);
        v.put("image", image);
        return getWritableDatabase().insert("Courts", null, v); // trả về rowId (=-1 nếu lỗi)
    }

    // Update
    public boolean updateCourt(int id, String name, String sport, String surface, boolean indoor, double price, @Nullable String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("sport", sport);
        v.put("surface", surface);
        v.put("indoor", indoor ? 1 : 0);
        v.put("price", price);
        if (image != null) v.put("image", image);
        return getWritableDatabase().update("Courts", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Delete
    public boolean deleteCourt(int id) {
        return getWritableDatabase().delete("Courts", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Get all (dùng cho Admin list)
    public List<Court> getAllCourts() {
        List<Court> list = new ArrayList<>();
        String sql = "SELECT id, name, sport, surface, indoor, price, image FROM Courts ORDER BY name";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                Court ct = new Court("Sân 7 người", 250000,"court_soccer");
                ct.id      = c.getInt(0);
                ct.name    = c.getString(1);
                ct.sport   = c.getString(2);
                ct.surface = c.getString(3);
                ct.indoor  = c.getInt(4);       // model đang dùng int 0/1
                ct.price   = c.getDouble(5);
                ct.image   = c.getString(6);
                list.add(ct);
            }
        }
        return list;
    }

    // Get by id (tiện cho màn sửa)
    @Nullable
    public Court getCourtById(int id) {
        String sql = "SELECT id, name, sport, surface, indoor, price, image FROM Courts WHERE id=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                Court ct = new Court("Sân 7 người", 250000, "court_soccer");
                ct.id = c.getInt(0);
                ct.name = c.getString(1);
                ct.sport = c.getString(2);
                ct.surface = c.getString(3);
                ct.indoor = c.getInt(4);
                ct.price = c.getDouble(5);
                ct.image = c.getString(6);
                return ct;
            }
        }
        return null;
    }

    // ===== PRODUCTS (ADMIN) =====

    // Create
    public long createProduct(String name, double price, int stock, @Nullable String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("price", price);
        v.put("stock", stock);
        v.put("image", image);
        return getWritableDatabase().insert("Products", null, v);
    }

    // Update
    public boolean updateProduct(int id, String name, double price, int stock, @Nullable String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("price", price);
        v.put("stock", stock);
        if (image != null) v.put("image", image);
        return getWritableDatabase().update("Products", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Delete
    public boolean deleteProduct(int id) {
        return getWritableDatabase().delete("Products", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Get all
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, price, stock, image FROM Products ORDER BY name";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                Product p = new Product();
                p.id    = c.getInt(0);
                p.name  = c.getString(1);
                p.price = c.getDouble(2);
                p.stock = c.getInt(3);
                p.image = c.getString(4);
                list.add(p);
            }
        }
        return list;
    }

    // Get by id
    @Nullable
    public Product getProductById(int id) {
        String sql = "SELECT id, name, price, stock, image FROM Products WHERE id=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                Product p = new Product();
                p.id    = c.getInt(0);
                p.name  = c.getString(1);
                p.price = c.getDouble(2);
                p.stock = c.getInt(3);
                p.image = c.getString(4);
                return p;
            }
        }
        return null;
    }


}
