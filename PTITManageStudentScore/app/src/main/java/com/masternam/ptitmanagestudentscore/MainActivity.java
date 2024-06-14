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
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Teacher;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    EditText emailEdt, passwordEdt;
    Button loginButton;

    Gson gson = new Gson();

    Teacher teacher = null;

    Student student = null;

    Thread teacherAndStudentFetchThread = null;

    CustomProgressDialog customProgressDialog = null;

    Button btnMissingPassword;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        btnMissingPassword = findViewById(R.id.missingPasswordTxt);

        customProgressDialog = new CustomProgressDialog(MainActivity.this);

//        Teacher teacher = AppUtils.getInfoSync(MainActivity.this);
        try {
            teacherAndStudentFetchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    teacher = AppUtils.getTeacherInfoSync(MainActivity.this);
                    student = AppUtils.getStudentInfoSync(MainActivity.this);
                    System.out.println(">>> teacher 1 from thread: " + teacher);
                }
            });
            teacherAndStudentFetchThread.start();
            teacherAndStudentFetchThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(">> teacher1: " + teacher);
        if (teacher != null) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.putExtra("teacherJson", gson.toJson(teacher));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            MainActivity.this.startActivity(intent);
            finish();
            return;
        }

        if (student != null) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.putExtra("studentJson", gson.toJson(student));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            MainActivity.this.startActivity(intent);
            finish();
            return;
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCallAPI();
            }
        });

        btnMissingPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MissingPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    private void clickCallAPI() {
        customProgressDialog.show();
        String email = emailEdt.getText().toString();
        String password = passwordEdt.getText().toString();

        System.out.println("email: " + email);
        System.out.println("password: " + password);

        if (!email.contains("@stu.ptit.edu.vn")) {
            teacherLogin(email, password);
            return;
        }
        studentLogin(email, password);
    }

    private void teacherLogin(String email , String password) {
        AuthenService.authenService.teacherLogin(email, password).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                ResponseCommonDto responseCommonDto = null;
                try {
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        System.out.println(">>> JSESSIONID faile: " + AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"));
                        AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }

                    System.out.println(">>> JSESSIONID success: " + AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"));
                    // save session id cookie
                    AppUtils.saveDataToSharedPreferences(
                            "JSESSIONID",
                            AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"),
                            MainActivity.this
                    );
                    try {
                        teacherAndStudentFetchThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                teacher = AppUtils.getTeacherInfoSync(MainActivity.this);
                            }
                        });
                        teacherAndStudentFetchThread.start();
                        teacherAndStudentFetchThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("teacher teacher: " + teacher);

                    // move to app main activity
                    intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.putExtra("teacherJson", gson.toJson(teacher));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                    finish();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void studentLogin(String email, String password) {
        AuthenService.authenService.studentLogin(email, password).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                ResponseCommonDto responseCommonDto = null;
                try {
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        System.out.println(">>> JSESSIONID faile: " + AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"));
                        AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }

                    System.out.println(">>> JSESSIONID success: " + AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"));
                    // save session id cookie
                    AppUtils.saveDataToSharedPreferences(
                            "JSESSIONID",
                            AppUtils.getCookie(response.headers().get("Set-Cookie"), "JSESSIONID"),
                            MainActivity.this
                    );
                    try {
                        teacherAndStudentFetchThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                student = AppUtils.getStudentInfoSync(MainActivity.this);
                            }
                        });
                        teacherAndStudentFetchThread.start();
                        teacherAndStudentFetchThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // move to app main activity
                    intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.putExtra("studentJson", gson.toJson(student));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                    finish();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(MainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println(">>> main resume");
    }
}