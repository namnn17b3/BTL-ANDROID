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

public interface SubjectService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    SubjectService subjectService = new Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SubjectService.class);

    @GET("api/teacher/subject")
    Call<ResponseCommonDto> getSubjects(@Header("Cookie") String jsessionId, @Query("termId") String termId);

    @GET("api/student/view-score")
    Call<ResponseCommonDto> getSubjectAndPoint(@Header("Cookie") String jsessionId, @Query("termId") String termId);
}
