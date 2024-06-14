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

public interface StudentService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    StudentService studentService = new Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(StudentService.class);

    @GET("api/teacher/student-point-table")
    Call<ResponseCommonDto> getStudents(
            @Header("Cookie") String jsessionId,
            @Query("classId") String classId,
            @Query("qText") String qText,
            @Query("page") String page);

    @GET("api/teacher/statistical")
    Call<ResponseCommonDto> getStatisticStudent(
            @Header("Cookie") String jsessionId,
            @Query("termId") String termId,
            @Query("subjectId") String subjectId);
}
