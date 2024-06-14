package com.masternam.ptitmanagestudentscore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissingPasswordActivity extends AppCompatActivity {

    EditText emailTxt, codeTxt, passwordTxt, confirmPasswordTxt;
    Button btnSendCode, btnLogin;
    CustomProgressDialog customProgressDialog;
    Intent intent;

    Gson gson = new Gson();

    boolean resetPasswordFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_password);

        emailTxt = findViewById(R.id.emailTxt);
        codeTxt = findViewById(R.id.codeTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        confirmPasswordTxt = findViewById(R.id.confirmPasswordTxt);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnLogin = findViewById(R.id.btn_login);

        customProgressDialog = new CustomProgressDialog(MissingPasswordActivity.this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MissingPasswordActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                MissingPasswordActivity.this.startActivity(intent);
                finish();
            }
        });

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText()+"";
                if (!resetPasswordFlag) sendCode(email);
                else resetPassword();
            }
        });
    }

    private void sendCode(String email) {
        customProgressDialog.show();
        AuthenService.authenService.forgotPassword(email).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                ResponseCommonDto responseCommonDto = null;
                try {
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        String message = responseCommonDto.getMessage();
                        AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, message);
                        if (message.equals("Mã code đã được gửi ở lần trước đó, vui lòng kiểm tra email")) {
                            emailTxt.setEnabled(false);
                            codeTxt.setVisibility(View.VISIBLE);
                            passwordTxt.setVisibility(View.VISIBLE);
                            confirmPasswordTxt.setVisibility(View.VISIBLE);
                            btnSendCode.setText("SEND");
                            resetPasswordFlag = true;
                        }
                        return;
                    }
                    emailTxt.setEnabled(false);
                    codeTxt.setVisibility(View.VISIBLE);
                    passwordTxt.setVisibility(View.VISIBLE);
                    confirmPasswordTxt.setVisibility(View.VISIBLE);
                    btnSendCode.setText("SEND");
                    resetPasswordFlag = true;
                    AppUtils.show(MissingPasswordActivity.this, R.id.successConstrainLayout, R.layout.success_dialog, R.id.successDesc, R.id.successDone, "Vui lòng kiểm tra mail để lấy mã code");
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void resetPassword() {
        String code = codeTxt.getText()+"";
        String password = passwordTxt.getText()+"";
        String confirmPassword = confirmPasswordTxt.getText()+"";

        customProgressDialog.show();
        AuthenService.authenService.resetPassword(code, password, confirmPassword).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                ResponseCommonDto responseCommonDto = null;
                try {
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        String message = responseCommonDto.getMessage();
                        AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, message);
                        return;
                    }
                    AppUtils.show(MissingPasswordActivity.this, R.id.successConstrainLayout, R.layout.success_dialog, R.id.successDesc, R.id.successDone, "Thay đổi mật khẩu thành công");
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(MissingPasswordActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }
}
