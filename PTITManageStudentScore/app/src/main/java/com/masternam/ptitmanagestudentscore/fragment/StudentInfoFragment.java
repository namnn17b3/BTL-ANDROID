package com.masternam.ptitmanagestudentscore.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentInfoFragment extends Fragment {
    TextView studentNameTxt, studentEmailTxt;
    AppCompatButton studentAddressTxt, studentPhoneTxt, studentDobTxt,
            studentGenderTxt, studentClassTxt;

    Gson gson = new Gson();

    String jsessionId;

    CustomProgressDialog customProgressDialog;

    Student student;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        studentNameTxt = view.findViewById(R.id.student_name_txt);
        studentEmailTxt = view.findViewById(R.id.student_email_txt);
        studentAddressTxt = view.findViewById(R.id.student_address_txt);
        studentPhoneTxt = view.findViewById(R.id.student_phone_txt);
        studentDobTxt = view.findViewById(R.id.student_dob_txt);
        studentGenderTxt = view.findViewById(R.id.student_gender_txt);
        studentClassTxt = view.findViewById(R.id.student_class_txt);

        customProgressDialog = new CustomProgressDialog(this.getContext());

        getStudentInfo();
    }

    private void getStudentInfo() {
        customProgressDialog.show();
        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", this.getContext());
        AuthenService.authenService.studentInfo("JSESSIONID="+jsessionId).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) StudentInfoFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    student = gson.fromJson(gson.toJson(responseCommonDto.getData()), Student.class);
                    bindingStudentToUI();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) StudentInfoFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) StudentInfoFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void bindingStudentToUI() {
        studentNameTxt.setText(student.getName());
        studentEmailTxt.setText(student.getEmail());
        studentAddressTxt.setText(student.getAddress());
        studentPhoneTxt.setText(student.getPhone());
        studentDobTxt.setText(AppUtils.stringifyFromStringDate(student.getDateOfBirth()));
        studentGenderTxt.setText(student.getGender());
        studentClassTxt.setText(student.getAdministrativeClass());
    }
}
