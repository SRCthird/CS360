package me.frigidambiance.projecttwo.net;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class ApiClient {

    private static String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    private ApiClient() {}

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        retrofit = null; // force rebuild
    }

    public static InventoryApi getInventoryApi(Context ctx) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient ok = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(ok)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit.create(InventoryApi.class);
    }
}