package com.masternam.ptitmanagestudentscore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.dto.res.TermAndSubjectsDto;
import java.util.UUID;

import com.masternam.ptitmanagestudentscore.fragment.ChangePasswordFragment;
import com.masternam.ptitmanagestudentscore.fragment.StudentInfoFragment;
import com.masternam.ptitmanagestudentscore.fragment.StudentViewScoreFragment;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Subject;
import com.masternam.ptitmanagestudentscore.model.Term;
import com.masternam.ptitmanagestudentscore.schedule.NotificationScheduler;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.service.TermService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;
import com.masternam.ptitmanagestudentscore.utils.MqttHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Gson gson = new Gson();

    private Student student = null;

    private Thread studentFetchThread = null;

    private Intent intent = null;

    private CustomProgressDialog customProgressDialog = null;

    private Fragment studentViewScoreFragment, studentInfoFragment, changePasswordFragement;

    private MqttHandler mqttHandler = new MqttHandler();

    public Student getStudent() {
        return this.student;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(StudentMainActivity.this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentViewScoreFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_student_view_score);
        }

        // binding studentInfo to nav header
        View headerView = navigationView.getHeaderView(0);
        TextView textViewUsername = headerView.findViewById(R.id.tv_username);
        TextView textViewEmail = headerView.findViewById(R.id.tv_email);

        intent = getIntent();
        String studentJson = intent.getStringExtra("studentJson");

        mqttHandler.connect(AppConstant.MQTT_URL, "subscriber-"+UUID.randomUUID());
        subscribeTopicMqtt();

        System.out.println(">>> studentJson: " + studentJson);
        if (studentJson != null) {
            student = this.gson.fromJson(studentJson, Student.class);
        } else {
            try {
                studentFetchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        student = AppUtils.getStudentInfoSync(StudentMainActivity.this);
                        System.out.println(">>> student from thread app main: " + student);
                    }
                });
                studentFetchThread.start();
                studentFetchThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("END");
        textViewUsername.setText(student.getName());
        textViewEmail.setText(student.getEmail());

        customProgressDialog = new CustomProgressDialog(StudentMainActivity.this);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_student_view_score) {
            studentViewScoreFragment = new StudentViewScoreFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, studentViewScoreFragment).commit();
        } else if (item.getItemId() == R.id.nav_student_info) {
            studentInfoFragment = new StudentInfoFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, studentInfoFragment).commit();
        } else if (item.getItemId() == R.id.nav_change_password) {
            changePasswordFragement = new ChangePasswordFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, changePasswordFragement).commit();
        } else {
            customProgressDialog.show();
            String jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", StudentMainActivity.this);
            AuthenService.authenService.logout("JSESSIONID="+jsessionId).enqueue(new Callback<ResponseCommonDto>() {
                @Override
                public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                    customProgressDialog.close();
                    AppUtils.removeDataToSharedPreferences("JSESSIONID", StudentMainActivity.this);
                    intent = new Intent(StudentMainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    StudentMainActivity.this.startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                    customProgressDialog.close();
                    AppUtils.show(StudentMainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                }
            });
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void subscribeTopicMqtt() {
        String jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", StudentMainActivity.this);
        TermService.termService.getStudentTermAndSubjects("JSESSIONID="+jsessionId).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(StudentMainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> list = (List<Object>) responseCommonDto.getData();
                    List<TermAndSubjectsDto> termAndSubjectsDtos = new ArrayList<>();
                    for (Object item : list) {
                        TermAndSubjectsDto termAndSubjectsDto = gson.fromJson(gson.toJson(item), TermAndSubjectsDto.class);
                        termAndSubjectsDtos.add(termAndSubjectsDto);
                    }
                    subscribeTopicMqtt(termAndSubjectsDtos);
                } catch (Exception e) {
                    AppUtils.show((Activity) StudentMainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                AppUtils.show((Activity) StudentMainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void subscribeTopicMqtt(List<TermAndSubjectsDto> termAndSubjectsDtos) {
        for (TermAndSubjectsDto termAndSubjectsDto : termAndSubjectsDtos) {
            Term term = termAndSubjectsDto.getTerm();
            List<Subject> subjects = termAndSubjectsDto.getSubjects();
            for (Subject subject : subjects) {
                String topic = term.getId()+"_"+subject.getId()+"_"+student.getId();
                mqttHandler.subscribe(topic, (tp, msg) -> {
                    byte[] payload = msg.getPayload();
                    String message = new String(payload, StandardCharsets.UTF_8);
                    NotificationScheduler.sendNotiSingleAtNow(StudentMainActivity.this, message);
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHandler.disconnect();
    }
}