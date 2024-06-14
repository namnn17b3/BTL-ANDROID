package com.masternam.ptitmanagestudentscore;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.dto.res.StatisticalStudentDto;
import com.masternam.ptitmanagestudentscore.service.StudentService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowChartActivity extends AppCompatActivity {

    BarChart chart;
    Intent intent;
    int termId, subjectId;

    String jsessionId;

    CustomProgressDialog customProgressDialog;

    Gson gson = new Gson();

    List<String> classNames = new ArrayList<>();
    List<BarEntry> accepts = new ArrayList<>();
    List<BarEntry> rejects = new ArrayList<>();

    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chart);

        chart = findViewById(R.id.chart);
        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", ShowChartActivity.this);
        customProgressDialog = new CustomProgressDialog(this);
        btnBack = findViewById(R.id.btn_back);

        intent = getIntent();
        termId = intent.getIntExtra("termId", 0);
        subjectId = intent.getIntExtra("subjectId", 0);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowChartActivity.this.finish();
            }
        });

        customProgressDialog.show();
        StudentService.studentService.getStatisticStudent(
        "JSESSIONID="+jsessionId,
            termId+"",
            subjectId+""
        ).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(ShowChartActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<StatisticalStudentDto> statisticalStudentDtos = new ArrayList<>();
                    List<Object> items = (List<Object>) responseCommonDto.getData();
                    for (Object item : items) {
                        StatisticalStudentDto statisticalStudentDto = gson.fromJson(
                            gson.toJson(item),
                            StatisticalStudentDto.class);
                        statisticalStudentDtos.add(statisticalStudentDto);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        renderChart(statisticalStudentDtos);
                    }
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(ShowChartActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(ShowChartActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void renderChart(List<StatisticalStudentDto> statisticalStudentDtos) {
        makeAccepts(statisticalStudentDtos);
        makeRejects(statisticalStudentDtos);
        makeCLassNames(statisticalStudentDtos);

        BarDataSet acceptDataSet = new BarDataSet(accepts, "Được thi");
        acceptDataSet.setColor(Color.argb(0.5f,54.0f/255,162.0f/255,235.0f/255));

        BarDataSet rejectDataSet = new BarDataSet(rejects, "Không được thi");
        rejectDataSet.setColor(Color.argb(0.5f,255.0f/255,99.0f/255,132.0f/255));

        BarData data = new BarData(acceptDataSet, rejectDataSet);
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(classNames));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(false);

        chart.setDragEnabled(true);
        chart.setVisibleXRangeMaximum(3);
        chart.getDescription().setText("Lớp");

        float barSpace = 0.05f;
        float groupSpace = 0.30f;
        data.setBarWidth(0.3f);

        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(0 + chart.getBarData().getGroupWidth(groupSpace, barSpace) * 5);
        chart.getAxisLeft().setAxisMinimum(0);

        chart.groupBars(0, groupSpace, barSpace);
        chart.invalidate();
    }

    private void makeCLassNames(List<StatisticalStudentDto> statisticalStudentDtos) {
        classNames.clear();
        for (StatisticalStudentDto statisticalStudentDto : statisticalStudentDtos) {
            classNames.add(statisticalStudentDto.getClassName());
        }
    }

    private void makeAccepts(List<StatisticalStudentDto> statisticalStudentDtos) {
        accepts.clear();
        int i = 0;
        for (StatisticalStudentDto statisticalStudentDto : statisticalStudentDtos) {
            accepts.add(new BarEntry(i, statisticalStudentDto.getAccept()));
            i++;
        }
    }

    private void makeRejects(List<StatisticalStudentDto> statisticalStudentDtos) {
        rejects.clear();
        int i = 0;
        for (StatisticalStudentDto statisticalStudentDto : statisticalStudentDtos) {
            rejects.add(new BarEntry(i, statisticalStudentDto.getReject()));
            i++;
        }
    }
}