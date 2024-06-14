package com.masternam.ptitmanagestudentscore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.dto.res.PointExtent;
import com.masternam.ptitmanagestudentscore.dto.res.StudentViewScoreDto;
import com.masternam.ptitmanagestudentscore.model.Point;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Subject;

public class StudentViewScoreDetailActivity extends AppCompatActivity {

    TextInputEditText ccTxt, btlTxt, thTxt,
            ktgkTxt, ktckTxt, scoreByNumberTxt, scoreByWordTxt,
            scorePerFourRankTxt, noteTxt;

    TextInputLayout ccTxtLayout, btlTxtLayout, thTxtLayout,
            ktgkTxtLayout, ktckTxtLayout;

    TextView msvTxt;
    Button btnBack, btnUpdate;

    Intent intent = null;

    Gson gson = new Gson();

    Student student;
    Point point;
    PointExtent pointExtent;
    Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_score_detail);

        ccTxt = findViewById(R.id.ccTxt);
        btlTxt = findViewById(R.id.btlTxt);
        thTxt = findViewById(R.id.thTxt);
        ktgkTxt = findViewById(R.id.ktgkTxt);
        ktckTxt = findViewById(R.id.ktckTxt);

        ccTxtLayout = findViewById(R.id.txt_layout_cc);
        btlTxtLayout = findViewById(R.id.txt_layout_btl);
        thTxtLayout = findViewById(R.id.txt_layout_th);
        ktgkTxtLayout = findViewById(R.id.txt_layout_ktgk);
        ktckTxtLayout = findViewById(R.id.txt_layout_ktck);

        scoreByNumberTxt = findViewById(R.id.scoreByNumberTxt);
        scoreByWordTxt = findViewById(R.id.scoreByWordTxt);
        scorePerFourRankTxt = findViewById(R.id.scorePerFourRankTxt);
        noteTxt = findViewById(R.id.noteTxt);

        msvTxt = findViewById(R.id.msv_title_txt);

        intent = getIntent();
        String studentViewScoreDtoJson = intent.getStringExtra("studentViewScoreDtoJson");
        String studentJson = intent.getStringExtra("studentJson");
        StudentViewScoreDto studentViewScoreDto = gson.fromJson(studentViewScoreDtoJson, StudentViewScoreDto.class);

        subject = studentViewScoreDto.getSubject();
        point = studentViewScoreDto.getPoint();
        pointExtent = studentViewScoreDto.getPointExtent();
        student = gson.fromJson(studentJson, Student.class);

        msvTxt.setText("Sinh viên SV"+student.getId());
        ccTxtLayout.setHint("CC "+subject.getPercentCC()+"%");
        btlTxtLayout.setHint("BTL "+subject.getPercentBTL()+"%");
        thTxtLayout.setHint("TH "+subject.getPercentTH()+"%");
        ktgkTxtLayout.setHint("KTGK "+subject.getPercentKTGK()+"%");
        ktckTxtLayout.setHint("KTCK "+subject.getPercentKTCK()+"%");

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentViewScoreDetailActivity.this.finish();
            }
        });

        bindingScore();
    }

    private void bindingScore() {
        if (subject.getPercentCC() > 0) {
            ccTxt.setText(point.getCc() != null ? point.getCc()+"" : "");
        }

        if (subject.getPercentBTL() > 0) {
            btlTxt.setText(point.getBtl() != null ? point.getBtl()+"" : "");
        }

        if (subject.getPercentTH() > 0) {
            thTxt.setText(point.getTh() != null ? point.getTh()+"" : "");
        }

        if (subject.getPercentKTGK() > 0) {
            ktgkTxt.setText(point.getKtgk() != null ? point.getKtgk()+"" : "");
        }

        if (subject.getPercentKTCK() > 0) {
            ktckTxt.setText(point.getKtck() != null ? point.getKtck()+"" : "");
        }

        if (pointExtent.getScoreByNumber() == null) return;

        scoreByNumberTxt.setText(pointExtent.getScoreByNumber()+"");
        scoreByWordTxt.setText(pointExtent.getScoreByWord());
        scorePerFourRankTxt.setText(pointExtent.getScorePerFourRank()+"");
        noteTxt.setText(pointExtent.getNote());

        setColorForScoreByWordTxt(pointExtent.getScoreByNumber());
    }

    private void setColorForScoreByWordTxt(Float score) {
        scorePerFourRankTxt.setTypeface(scorePerFourRankTxt.getTypeface(), Typeface.BOLD);
        scoreByWordTxt.setTypeface(scorePerFourRankTxt.getTypeface(), Typeface.BOLD);
        scoreByNumberTxt.setTypeface(scorePerFourRankTxt.getTypeface(), Typeface.BOLD);
        noteTxt.setTypeface(scorePerFourRankTxt.getTypeface(), Typeface.BOLD);
        if (score < 4.0) {
            scoreByWordTxt.setTextColor(getResources().getColor(R.color.danger));
        } else if (score >= 4.0 && score < 7) {
            scoreByWordTxt.setTextColor(getResources().getColor(R.color.warning));
        } else if (score >= 7 && score <= 10) {
            scoreByWordTxt.setTextColor(getResources().getColor(R.color.success));
        }
    }
}