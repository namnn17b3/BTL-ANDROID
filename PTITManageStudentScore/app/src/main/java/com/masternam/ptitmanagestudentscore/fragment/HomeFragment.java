package com.masternam.ptitmanagestudentscore.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.model.Term;
import com.masternam.ptitmanagestudentscore.schedule.NotificationScheduler;
import com.masternam.ptitmanagestudentscore.service.TermService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    String jsessionId;
    CustomProgressDialog customProgressDialog;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", HomeFragment.this.getContext());
        customProgressDialog = new CustomProgressDialog(HomeFragment.this.getContext());
        saveScheduleSendNotification();
    }

    private void saveScheduleSendNotification() {
        customProgressDialog.show();
        TermService.termService.getTeacherTerms("JSESSIONID="+jsessionId, "0").enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) HomeFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> tmp = (List<Object>) responseCommonDto.getData();
                    for (Object item : tmp) {
                        Term term = gson.fromJson(gson.toJson(item), Term.class);
                        schedule(term);
                    }
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) HomeFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) HomeFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void schedule(Term term) {
        String dateString = AppUtils.stringifyFromStringDate(term.getEndDate());
        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date startDate = simpleDateFormat.parse(dateString);

            // Tạo một đối tượng Calendar và thiết lập ngày ban đầu
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            // Lùi ngày 7 ngày
            calendar.add(Calendar.DAY_OF_MONTH, -7);

            NotificationScheduler.scheduleNotifications(
                    HomeFragment.this.getContext(),
                    calendar,
                    8, 31, 0,
                    "Nhắc nhở cập nhật điểm sinh viên học kì "+term.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
