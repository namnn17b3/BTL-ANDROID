package com.masternam.ptitmanagestudentscore.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    String jsessionId;
    TextInputEditText oldPasswordTxt, newPasswordTxt, confirmNewPasswordTxt;
    Button btnUpdate;
    CustomProgressDialog customProgressDialog;

    Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", ChangePasswordFragment.this.getContext());
        customProgressDialog = new CustomProgressDialog(ChangePasswordFragment.this.getContext());

        oldPasswordTxt = view.findViewById(R.id.oldPasswordTxt);
        newPasswordTxt = view.findViewById(R.id.newPasswordTxt);
        confirmNewPasswordTxt = view.findViewById(R.id.confirmNewPasswordTxt);

        btnUpdate = view.findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordTxt.getText()+"";
                String newPassword = newPasswordTxt.getText()+"";
                String confirmNewPassword = confirmNewPasswordTxt.getText()+"";
                changePassword(oldPassword, newPassword, confirmNewPassword);
            }
        });
    }

    private void changePassword(String oldPassword, String newPassword, String confirmNewPassword) {
        customProgressDialog.show();
        AuthenService.authenService.changePassword(
                "JSESSIONID="+jsessionId,
                oldPassword,
                newPassword,
                confirmNewPassword).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                ResponseCommonDto responseCommonDto = null;
                try {
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        String message = responseCommonDto.getMessage();
                        AppUtils.show((Activity) ChangePasswordFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, message);
                        return;
                    }
                    AppUtils.show((Activity) ChangePasswordFragment.this.getContext(), R.id.successConstrainLayout, R.layout.success_dialog, R.id.successDesc, R.id.successDone, "Thay đổi mật khẩu thành công");
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) ChangePasswordFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) ChangePasswordFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }
}
