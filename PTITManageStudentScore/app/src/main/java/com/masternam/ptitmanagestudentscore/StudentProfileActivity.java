package com.masternam.ptitmanagestudentscore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

public class StudentProfileActivity extends AppCompatActivity {

    Button btnBack;
    TextView studentNameTxt, studentEmailTxt;
    AppCompatButton studentAddressTxt, studentPhoneTxt, studentDobTxt,
        studentGenderTxt, studentClassTxt;

    Gson gson = new Gson();
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        btnBack = findViewById(R.id.btn_back);
        studentNameTxt = findViewById(R.id.student_name_txt);
        studentEmailTxt = findViewById(R.id.student_email_txt);
        studentAddressTxt = findViewById(R.id.student_address_txt);
        studentPhoneTxt = findViewById(R.id.student_phone_txt);
        studentDobTxt = findViewById(R.id.student_dob_txt);
        studentGenderTxt = findViewById(R.id.student_gender_txt);
        studentClassTxt = findViewById(R.id.student_class_txt);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentProfileActivity.this.finish();
            }
        });

        intent = getIntent();
        String studentJson = intent.getStringExtra("studentJson");
        Student student = gson.fromJson(studentJson, Student.class);

        studentNameTxt.setText(student.getName());
        studentEmailTxt.setText(student.getEmail());
        studentAddressTxt.setText(student.getAddress());
        studentPhoneTxt.setText(student.getPhone());
        studentDobTxt.setText(AppUtils.stringifyFromStringDate(student.getDateOfBirth()));
        studentGenderTxt.setText(student.getGender());
        studentClassTxt.setText(student.getAdministrativeClass());
    }
}