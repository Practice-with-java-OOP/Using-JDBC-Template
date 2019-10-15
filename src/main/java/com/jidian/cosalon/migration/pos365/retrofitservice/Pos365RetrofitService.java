package com.jidian.cosalon.migration.pos365.retrofitservice;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Transfer;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Pos365RetrofitService {

    @POST("auth/credentials?format=json")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("branchs?format=json")
    Call<BaseResponse<Pos365Branch>> listBranchs(@HeaderMap Map<String, String> headers);

    @GET("users?format=json")
    Call<BaseResponse<Pos365User>> listUsers(@HeaderMap Map<String, String> headers,
        @Query("top") Integer top, @Query("skip") Integer skip);

    @GET("https://cosalon.pos365.vn/api/products?Type=1&CategoryId=-1")
    Call<BaseResponse<Pos365Product>> listProducts(@HeaderMap Map<String, String> headers,
        @Query("top") Integer top, @Query("skip") Integer skip);

    @GET("transfers")
    Call<BaseResponse<Pos365Transfer>> listTransfers(@HeaderMap Map<String, String> headers,
        @Query("top") Integer top, @Query("skip") Integer skip);
}
