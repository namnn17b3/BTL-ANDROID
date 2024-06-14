package com.masternam.ptitmanagestudentscore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Teacher;

public class WelcomeActivity extends AppCompatActivity {

    Gson gson = new Gson();
    TextView welcomeTv;

    Intent intent;

    Teacher teacher;

    Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        intent = getIntent();
        String teacherJson = intent.getStringExtra("teacherJson");
        String studentJson = intent.getStringExtra("studentJson");


        intent.putExtra("teacherJson", teacherJson);
        intent.putExtra("studentJson", studentJson);

        teacher = this.gson.fromJson(teacherJson, Teacher.class);
        student = this.gson.fromJson(studentJson, Student.class);

        welcomeTv = findViewById(R.id.welcome_txt);
        if (teacher != null) {
            welcomeTv.setText("Xin chào, " + teacher.getName());
            intent = new Intent(WelcomeActivity.this, TeacherMainActivity.class);
        }

        if (student != null) {
            welcomeTv.setText("Xin chào, "+student.getName());
            intent = new Intent(WelcomeActivity.this, StudentMainActivity.class);
        }

        // Tạo một đối tượng Handler
        Handler handler = new Handler();

        // Sử dụng phương thức postDelayed() để chạy một Runnable sau 2 giây
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                WelcomeActivity.this.startActivity(intent);
                finish();
            }
        }, 2000);
    }
}