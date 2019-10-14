package com.jidian.cosalon.migration.pos365.config;

import com.jidian.cosalon.migration.pos365.Utils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class RetrofitConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Retrofit");
    private static final int DEFAULT_TIME_OUT = 180; // giay

    @Bean
    public Retrofit retrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor headerInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json; charset=utf-8")
                    .addHeader("Cookie", "ss-id=" + Utils.SESSION_ID)
                    .method(originalRequest.method(), originalRequest.body());
            if( originalRequest.body() != null ){
                requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
            }
            Request request = requestBuilder.build();
            LOGGER.debug("requestHeader = " + request.headers().toString());
            return chain.proceed(request);
        };
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .callTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(headerInterceptor);
        return new Retrofit.Builder()
                .baseUrl("https://cosalon.pos365.vn/api/")
                .client(okHttpBuilder.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
