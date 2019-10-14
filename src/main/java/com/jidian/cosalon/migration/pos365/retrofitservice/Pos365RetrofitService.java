package com.jidian.cosalon.migration.pos365.retrofitservice;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.dto.BranchResponse;
import com.jidian.cosalon.migration.pos365.dto.LoginRequest;
import com.jidian.cosalon.migration.pos365.dto.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.Map;

public interface Pos365RetrofitService {

    @POST("auth/credentials?format=json")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("branchs?format=json")
    Call<BaseResponse<BranchResponse>> listBranchs(@HeaderMap Map<String, String> headers);
}
