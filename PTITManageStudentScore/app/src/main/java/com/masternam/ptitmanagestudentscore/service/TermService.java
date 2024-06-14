package com.masternam.ptitmanagestudentscore.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TermService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    TermService termService = new Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TermService.class);

    @GET("api/teacher/term")
    Call<ResponseCommonDto> getTeacherTerms(@Header("Cookie") String jsessionId, @Query("lt") String lt);

    @GET("api/student/term")
    Call<ResponseCommonDto> getStudentTerms(@Header("Cookie") String jsessionId);

    @GET("api/student/term-subjects")
    Call<ResponseCommonDto> getStudentTermAndSubjects(@Header("Cookie") String jsessionId);
}
