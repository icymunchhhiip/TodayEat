package com.jinsun.todayeat.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HttpClient {
    public static final String BASE_URL_SERVER  = "http://ddony.cafe24.com/";
    public static final String BASE_URL_TMAP    = "https://apis.openapi.sk.com/tmap/";
    private static Retrofit retrofitTmap = null;
    private static Retrofit retrofitServer = null;

    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    public static Retrofit getServerClient() {
        if (retrofitServer == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            retrofitServer = new Retrofit.Builder()
                    .baseUrl(BASE_URL_SERVER)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofitServer;
    }

    public static Retrofit getTmapClient() {
        if (retrofitTmap == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            retrofitTmap = new Retrofit.Builder()
                    .baseUrl(BASE_URL_TMAP)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofitTmap;
    }
}
