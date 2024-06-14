package com.masternam.ptitmanagestudentscore.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface PointService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    PointService pointService = new Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PointService.class);

    @GET("api/teacher/point")
    Call<ResponseCommonDto> getPointInfo(
            @Header("Cookie") String jsessionId,
            @Query("classId") String classId,
            @Query("studentId") String studentId);

    @FormUrlEncoded
    @PUT("api/teacher/save-point")
    Call<ResponseCommonDto> updatePoint(
            @Header("Cookie") String jsessionId,
            @Field("pointId") @NonNull String pointId,
            @Field("cc") @Nullable String cc,
            @Field("btl") @Nullable String btl,
            @Field("th") @Nullable String th,
            @Field("ktgk") @Nullable String ktgk,
            @Field("ktck") @Nullable String ktck);

    @Multipart
    @POST("api/teacher/import-excel")
    Call<ResponseCommonDto> uploadFile(
            @Header("Cookie") String jsessionId,
            @Query("classId") String classId,
            @Part MultipartBody.Part file);
}
