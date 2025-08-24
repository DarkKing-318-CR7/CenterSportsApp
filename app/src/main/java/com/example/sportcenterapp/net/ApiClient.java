package com.example.sportcenterapp.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static Retrofit instance;
    public static final String BASE_URL = "http://10.0.2.2/sportcenter_api/";

    public static Retrofit build() {
        if (retrofit == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient ok = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();
            Gson gson = new GsonBuilder()
                    .setLenient()   // <— cho phép đọc JSON “dơ” để không crash khi debug
                    .create();


            retrofit = new Retrofit.Builder()
                    // Emulator Android Studio → dùng 10.0.2.2. Nếu chạy trên máy thật cùng Wi-Fi, đổi thành IP PC (vd 192.168.1.x)
                    .baseUrl("http://10.0.2.2/sportcenter_api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(ok)
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getInstance() {
        if (instance == null) instance = build();
        return instance;
    }

    /** Nếu muốn gọi dạng getClient() */
    public static Retrofit getClient() { return getInstance(); }
}
