package com.masternam.ptitmanagestudentscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.fragment.ChangePasswordFragment;
import com.masternam.ptitmanagestudentscore.fragment.EnterPointFragment;
import com.masternam.ptitmanagestudentscore.fragment.HomeFragment;
import com.masternam.ptitmanagestudentscore.fragment.StatisticPointFragment;
import com.masternam.ptitmanagestudentscore.model.Teacher;
import com.masternam.ptitmanagestudentscore.service.AuthenService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Gson gson = new Gson();

    private Teacher teacher = null;

    private Thread teacherFetchThread = null;

    private Intent intent = null;

    private CustomProgressDialog customProgressDialog = null;

    private Fragment homeFragment, enterPointFragment,
            statisticPointFragment, changePasswordFragement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(TeacherMainActivity.this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // binding teacherInfo to nav header
        View headerView = navigationView.getHeaderView(0);
        TextView textViewUsername = headerView.findViewById(R.id.tv_username);
        TextView textViewEmail = headerView.findViewById(R.id.tv_email);

        intent = getIntent();
        String teacherJson = intent.getStringExtra("teacherJson");

        System.out.println(">>> teacherJson: " + teacherJson);
        if (teacherJson != null) {
            teacher = this.gson.fromJson(teacherJson, Teacher.class);
        } else {
            try {
                teacherFetchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        teacher = AppUtils.getTeacherInfoSync(TeacherMainActivity.this);
                        System.out.println(">>> teacher from thread app main: " + teacher);
                    }
                });
                teacherFetchThread.start();
                teacherFetchThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("END");
        textViewUsername.setText(teacher.getName());
        textViewEmail.setText(teacher.getEmail());

        customProgressDialog = new CustomProgressDialog(TeacherMainActivity.this);

        homeFragment = new HomeFragment();
//        enterPointFragment = new EnterPointFragment();
//        statisticPointFragment = new StatisticPointFragment();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
//            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        } else if (item.getItemId() == R.id.nav_enter_point) {
            enterPointFragment = new EnterPointFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, enterPointFragment).commit();
        } else if (item.getItemId() == R.id.nav_statistic_student) {
            statisticPointFragment = new StatisticPointFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, statisticPointFragment).commit();
        } else if (item.getItemId() == R.id.nav_change_password) {
            changePasswordFragement = new ChangePasswordFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, changePasswordFragement).commit();
        } else {
            customProgressDialog.show();
            String jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", TeacherMainActivity.this);
            AuthenService.authenService.logout("JSESSIONID="+jsessionId).enqueue(new Callback<ResponseCommonDto>() {
                @Override
                public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                    customProgressDialog.close();
                    AppUtils.removeDataToSharedPreferences("JSESSIONID", TeacherMainActivity.this);
                    intent = new Intent(TeacherMainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    TeacherMainActivity.this.startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                    customProgressDialog.close();
                    AppUtils.show(TeacherMainActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                }
            });
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
