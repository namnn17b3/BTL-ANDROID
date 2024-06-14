package com.masternam.ptitmanagestudentscore.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.converter.gson.GsonConverterFactory;

public interface AuthenService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    AuthenService authenService = new Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthenService.class);

    @FormUrlEncoded
    @POST("api/teacher/login")
    Call<ResponseCommonDto> teacherLogin(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("api/student/login")
    Call<ResponseCommonDto> studentLogin(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("api/teacher/info")
    Call<ResponseCommonDto> teacherInfo(@Header("Cookie") String jsessionId);

    @GET("api/student/info")
    Call<ResponseCommonDto> studentInfo(@Header("Cookie") String jsessionId);

    @GET("api/teacher/logout")
    Call<ResponseCommonDto> logout(@Header("Cookie") String jsessionId);

    @FormUrlEncoded
    @POST("api/teacher/forgot-password")
    Call<ResponseCommonDto> forgotPassword(@Field("email") String email);

    @FormUrlEncoded
    @POST("api/teacher/reset-password")
    Call<ResponseCommonDto> resetPassword(
            @Field("code") String code,
            @Field("password") String password,
            @Field("confirmPassword") String confirmPassword);

    @FormUrlEncoded
    @POST("api/change-password")
    Call<ResponseCommonDto> changePassword(
            @Header("Cookie") String jsessionId,
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword,
            @Field("confirmNewPassword") String confirmNewPassword);
}
