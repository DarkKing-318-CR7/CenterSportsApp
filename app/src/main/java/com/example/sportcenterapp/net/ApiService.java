package com.example.sportcenterapp.net;

import com.example.sportcenterapp.models.Booking;
import com.example.sportcenterapp.models.Coach;
import com.example.sportcenterapp.models.OrderItem;
import com.example.sportcenterapp.models.Product;
import com.example.sportcenterapp.models.User;
import com.google.gson.annotations.SerializedName;
import com.example.sportcenterapp.models.Court;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
import okhttp3.MultipartBody;

// Bạn đã có models.User/Court; nếu field không trùng tên JSON, dùng lớp “DTO” nhỏ map sẵn như dưới.
public interface ApiService {

    class LoginReq {
        public String username, password;
        public LoginReq(String u, String p){ this.username=u; this.password=p; }
    }

    class UserDTO {
        public int id;
        public String username;
        @SerializedName("full_name") public String fullName;
        public String role;
        public int vip;
    }

    @POST("auth_login.php")
    Call<UserDTO> login(@Body LoginReq req);

    @GET("courts_list.php")
    Call<List<Court>> getCourts(@Query("q") String q);

    // com.example.sportcenterapp.net.ApiService

    @POST("bookings_create.php")
    Call<BookingCreateResp> createBooking(@Body BookingCreateReq req);

    @GET("bookings_list_by_user.php")
    Call<List<Booking>> getBookingsByUser(@Query("user_id") int userId);

    @GET("bookings_list_admin.php")
    Call<List<BookingAdmin>> getBookingsAdmin(
            @Query("status") String status,   // "", "PENDING", "CONFIRMED", "CANCELLED"
            @Query("when")   String when      // "", "today", "upcoming", "past"
    );

    @GET("bookings_list_admin.php")
    Call<List<BookingAdmin>> getBookingsAdmin(@Query("status") String status); // "", "PENDING",...

    @POST("bookings_update_status.php")
    Call<SimpleResp> updateBookingStatus(@Body UpdateStatusReq req);

    @POST("bookings_cancel_by_user.php")
    Call<SimpleResp> cancelBookingByUser(@Body CancelReq req);

    @GET("bookings_taken_by_court_date.php")
    Call<List<TakenSlot>> getTakenSlots(
            @Query("court_id") int courtId,
            @Query("date") String date // yyyy-MM-dd
    );

    @POST("courts_save.php")
    Call<ApiService.SimpleRespId> saveCourt(@Body ApiService.CourtSaveReq req);

    @POST("courts_delete.php")
    Call<ApiService.SimpleResp> deleteCourt(@Body ApiService.IdReq req);

    @Multipart
    @POST("courts_upload_image.php")
    Call<ApiService.UploadImageResp> uploadCourtImage(
            @Part("court_id") okhttp3.RequestBody courtId,
            @Part okhttp3.MultipartBody.Part image
    );

    // DTO tối thiểu (nếu chưa có trong models):
    class BookingCreateReq {
        public int user_id, court_id;
        public String date, start_time, end_time;
        public double total_price;
        // đặt sân
        public BookingCreateReq(int user_id, int court_id, String date, String start_time, String end_time, double total_price){
            this.user_id=user_id; this.court_id=court_id; this.date=date;
            this.start_time=start_time; this.end_time=end_time; this.total_price=total_price;
        }
    }
    class BookingCreateResp { public boolean ok; public int booking_id; public String error; }

    //Lịch sử đặt sân
    class Booking {
        public int id, user_id, court_id;
        public String date, start_time, end_time, status, created_at, court_name;
        public double total_price;
        public String image;
    }

    class TakenSlot {
        public String start_time;
        public String end_time;
    }

    class BookingAdmin {
        public int id, user_id, court_id;
        public String user_name, court_name, image;
        public String date, start_time, end_time, status, created_at;
        public double total_price;
    }
    class UpdateStatusReq {
        public int booking_id; public String status;
        public UpdateStatusReq(int id, String s){ booking_id=id; status=s; }
    }
    class SimpleResp { public boolean ok; public String error; }

    class CancelReq { public int user_id, booking_id;
        public CancelReq(int u, int b){ user_id=u; booking_id=b; }
    }
    class CourtSaveReq {
        // id = null => create; có id => update
        public Integer id;
        public String name;
        public String sport;
        public String surface;
        public int indoor;         // 0/1
        public double price;       // KHỚP Court.java của bạn
        public String description; // KHỚP Court.java của bạn
        public double rating;
    }
    class IdReq { public int id; public IdReq(int id){ this.id=id; } }
    class SimpleRespId { public boolean ok; public String error; public int id; }
    class UploadImageResp { public boolean ok; public String error; public String image; }


    @GET("products_list.php")
    Call<List<Product>> getProducts(@Query("q") String q);

    @POST("products_save.php")
    Call<SimpleRespId> saveProduct(@Body ProductSaveReq req);

    @Multipart
    @POST("products_upload_image.php")
    Call<UploadImageResp> uploadProductImage(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part image
    );

    @POST("products_delete.php")
    Call<SimpleResp> deleteProduct(@Body IdReq req);

    // DTO
    class ProductSaveReq {
        public Integer id;
        public String name, category, description;
        public double price;
        public int stock;
        public String status;// 1=ACTIVE, 0=HIDDEN
    }

    // === SHOP / CART / ORDERS ===
    @POST("orders_create.php")
    Call<SimpleRespId> createOrder(@Body OrderCreateReq req);

    @GET("orders_list_by_user.php")
    Call<List<OrderDTO>> getOrdersByUser(@Query("user_id") int userId);

    @GET("order_items_by_order.php")
    Call<List<OrderItem>> getOrderItems(@Query("order_id") int orderId);

    @GET("orders_list_all.php")
    Call<List<ApiService.OrderDTO>> adminGetAllOrders();

    @GET("orders_detail.php")
    Call<ApiService.OrderDTO> adminGetOrderDetail(@Query("order_id") int orderId);

    @GET("orders_list_admin.php")
    Call<List<OrderAdminDTO>> getOrdersAdmin(@Query("status") String status);

    @POST("orders_update_status.php")
    Call<ApiService.SimpleResp> adminUpdateOrderStatus(
            @Query("order_id") int orderId,
            @Query("status") String status );// PENDING | APPROVED | REJECTED | CANCELED

    @POST("orders_update_status.php")
    Call<SimpleResp> updateOrderStatus(@Body OrderStatusUpdateReq body);



    // ==== DTOs ====
    class OrderCreateReq {
        public int user_id;
        public List<Item> items;
        public static class Item {
            public int product_id;
            public int qty;
            public double price;
            public String name;  // tùy, có thể bỏ nếu server không cần
        }
    }
    class CreateOrderResp { public boolean ok; public int order_id; public String error; }
    // Nếu bạn chưa có:
    class Order {
        public int id, user_id;
        public double total;
        public String status, created_at;
        public List<OrderItem> items;
    }
    public static class OrderDTO {
        public Integer id;
        public String status;
        public String created_at;

        @com.google.gson.annotations.SerializedName("total_price")
        public Double totalPrice;   // Gson sẽ tự map "total_price" -> totalPrice
    }


    public static class OrderAdminDTO {
        @SerializedName("id") public int id;

        // tên người dùng có thể là user_name / username / user / name
        @SerializedName(value = "user_name", alternate = {"username", "user", "name"})
        public String user_name;

        // id người dùng có thể là user_id / uid / userId
        @SerializedName(value = "user_id", alternate = {"uid", "userId"})
        public Integer user_id;

        @SerializedName("status") public String status;
        @SerializedName("created_at") public String created_at;

        // có thể là String hoặc Number nên để Object
        @SerializedName("total") public Object total;
    }
    class OrderStatusUpdateReq { public int order_id; public String status; }
    class OrderItem {
        public int id;          // id bản ghi order_items (nếu có)
        public int product_id;
        public String name;     // tên sản phẩm (server nên JOIN và trả sẵn)
        public Object price;    // có thể số / chuỗi -> để Object
        public int qty;
        public String image;    // (optional) đường dẫn ảnh, nếu server trả
    }

    @GET("products_list_admin.php")
    Call<ProductListResponse> adminProducts(
            @Query("q") String q,
            @Query("category") String category,
            @Query("status") String status,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("sort") String sort,
            @Query("order") String order
    );

    // CREATE
    @POST("product_create.php")
    Call<BaseResponse> createProduct(@Body ProductCreateRequest body);

    // UPDATE
    @POST("product_update.php")
    Call<BaseResponse> updateProduct(@Query("id") int id, @Body ProductUpdateRequest body);

    // DELETE
    @POST("product_delete.php")
    Call<BaseResponse> deleteProduct(@Query("id") int id);

    // UPLOAD (nếu dùng)
    @Multipart
    @POST("product_upload_image.php")
    Call<ImageUploadResponse> uploadProductImage(@Part MultipartBody.Part file);

    public class ProductListResponse { public int page, limit, total; public List<Product> items; }
    public class BaseResponse { public boolean success; public String error; public Integer id; }
    public class ProductCreateRequest {
        public String name, description, category, image_url, status; public double price; public int stock;
    }
    public class ProductUpdateRequest {
        public String name, description, category, image_url, status; public Double price; public Integer stock;
    }
    public class ImageUploadResponse {
        public boolean success;
        public String url;     // PHP trả về đường dẫn ảnh, ví dụ "/images/products/xxx.png"
        public String error;   // nếu có lỗi
    }

    // ===== Player Account =====
    @GET("user_profile.php")
    Call<UserResponse> getUserProfile(@Query("id") int userId);

    @POST("user_update.php")
    Call<BaseResponse> updateUser(@Query("id") int userId, @Body UserUpdateRequest body);

    @POST("user_change_password.php")
    Call<BaseResponse> changePassword(@Query("id") int userId, @Body ChangePasswordRequest body);

    public class UserResponse { public boolean success; public User user; }
    public class UserUpdateRequest {
        @SerializedName("full_name")  public String fullName;
        @SerializedName("phone")      public String phone;
        @SerializedName("address")    public String address;
        @SerializedName("avatar_url") public String avatar;
    }

    public class ChangePasswordRequest {
        @SerializedName("old_password") public String oldPassword;
        @SerializedName("new_password") public String newPassword;
    }

    @GET("products_list_public.php")
    Call<List<Product>> productsPublic(@Query("q") String q,
                                       @Query("page") Integer page,
                                       @Query("limit") Integer limit);

    @GET("coaches_list.php")
    Call<List<Coach>> getCoaches(@Query("sport") String sport);

    @GET("coaches_sports.php")
    Call<List<String>> getCoachSports();

    class CoachSaveReq {
        public Integer id;  // null => create
        public String name, sport, level, avatar, bio, phone, email, zalo;
        @SerializedName("rate_per_hour") public double ratePerHour; // map để PHP nhận field snake_case
    }

    @POST("coach_save.php")
    Call<SimpleRespId> saveCoach(@Body CoachSaveReq req);

    @POST("coach_delete.php")
    Call<SimpleResp> deleteCoach(@Body IdReq req);

    @Multipart
    @POST("coach_upload_avatar.php")
    Call<UploadImageResp> uploadCoachAvatar(
            @Part("coach_id") RequestBody coachId,
            @Part MultipartBody.Part image
    );

}
