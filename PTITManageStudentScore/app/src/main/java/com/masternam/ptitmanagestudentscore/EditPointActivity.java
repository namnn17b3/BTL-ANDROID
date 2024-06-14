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
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.PointExtent;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.dto.res.SavePointResponseDto;
import com.masternam.ptitmanagestudentscore.dto.res.StudentPointTable;
import com.masternam.ptitmanagestudentscore.model.Point;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Subject;
import com.masternam.ptitmanagestudentscore.service.PointService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPointActivity extends AppCompatActivity {

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

    String jsessionId;

    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_point);

        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", this);
        customProgressDialog = new CustomProgressDialog(this);

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

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPointActivity.this.finish();
            }
        });

        btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePoint();
            }
        });

        intent = getIntent();
        String subjectJson = intent.getStringExtra("subjectJson");
        String studentJson = intent.getStringExtra("studentJson");
        String pointJson = intent.getStringExtra("pointJson");
        String pointExtentJson = intent.getStringExtra("pointExtentJson");

        student = gson.fromJson(studentJson, Student.class);
        point = gson.fromJson(pointJson, Point.class);
        pointExtent = gson.fromJson(pointExtentJson, PointExtent.class);
        subject = gson.fromJson(subjectJson, Subject.class);

        msvTxt.setText("Sinh viên SV"+student.getId());

        ccTxtLayout.setHint("CC "+subject.getPercentCC()+"%");
        btlTxtLayout.setHint("BTL "+subject.getPercentBTL()+"%");
        thTxtLayout.setHint("TH "+subject.getPercentTH()+"%");
        ktgkTxtLayout.setHint("KTGK "+subject.getPercentKTGK()+"%");
        ktckTxtLayout.setHint("KTCK "+subject.getPercentKTCK()+"%");

        getPointInfo();
    }

    private void bindingScore() {
        if (subject.getPercentCC() > 0) {
            ccTxt.setText(point.getCc() != null ? point.getCc()+"" : "");
        } else {
            ccTxt.setEnabled(false);
        }

        if (subject.getPercentBTL() > 0) {
            btlTxt.setText(point.getBtl() != null ? point.getBtl()+"" : "");
        } else {
            btlTxt.setEnabled(false);
        }

        if (subject.getPercentTH() > 0) {
            thTxt.setText(point.getTh() != null ? point.getTh()+"" : "");
        } else {
            thTxt.setEnabled(false);
        }

        if (subject.getPercentKTGK() > 0) {
            ktgkTxt.setText(point.getKtgk() != null ? point.getKtgk()+"" : "");
        } else {
            ktgkTxt.setEnabled(false);
        }

        if (subject.getPercentKTCK() > 0) {
            ktckTxt.setText(point.getKtck() != null ? point.getKtck()+"" : "");
        } else {
            ktckTxt.setEnabled(false);
        }

        if (pointExtent.getScoreByNumber() == null) return;

        scoreByNumberTxt.setText(pointExtent.getScoreByNumber()+"");
        scoreByWordTxt.setText(pointExtent.getScoreByWord());
        scorePerFourRankTxt.setText(pointExtent.getScorePerFourRank()+"");
        noteTxt.setText(pointExtent.getNote());

        setColorForScoreByWordTxt(pointExtent.getScoreByNumber());
    }

    private void getPointInfo() {
        customProgressDialog.show();
        PointService.pointService.getPointInfo("JSESSIONID="+jsessionId,
                point.getClassId()+"", point.getStudentId()+"")
                .enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    StudentPointTable studentPointTable = gson.fromJson(gson.toJson(responseCommonDto.getData()), StudentPointTable.class);
                    point = studentPointTable.getPoint();
                    pointExtent = studentPointTable.getPointExtent();
                    bindingScore();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
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

    private void updatePoint() {
        String cc = ccTxt.getText()+"";
        String btl = btlTxt.getText()+"";
        String th = thTxt.getText()+"";
        String ktgk = ktgkTxt.getText()+"";
        String ktck = ktckTxt.getText()+"";

        PointService.pointService.updatePoint("JSESSIONID="+jsessionId,
                point.getId()+"", cc, btl, th, ktgk, ktck).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    SavePointResponseDto studentPointTable = gson.fromJson(gson.toJson(responseCommonDto.getData()), SavePointResponseDto.class);

                    point.setCc(studentPointTable.getCc());
                    point.setBtl(studentPointTable.getBtl());
                    point.setTh(studentPointTable.getTh());
                    point.setKtgk(studentPointTable.getKtgk());
                    point.setKtck(studentPointTable.getKtck());

                    pointExtent.setScoreByNumber(studentPointTable.getScoreByNumber());
                    pointExtent.setScoreByWord(studentPointTable.getScoreByWord());
                    pointExtent.setScorePerFourRank(studentPointTable.getScorePerFourRank());
                    pointExtent.setNote(studentPointTable.getNote());

                    bindingScore();
                    AppUtils.show(EditPointActivity.this, R.id.successConstrainLayout, R.layout.success_dialog, R.id.successDesc, R.id.successDone, "Cập nhật điểm thành công");
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(EditPointActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }
}