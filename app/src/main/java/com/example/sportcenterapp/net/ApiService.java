package com.example.sportcenterapp.net;

import com.example.sportcenterapp.models.Booking;
import com.google.gson.annotations.SerializedName;
import com.example.sportcenterapp.models.Court;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

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

}
