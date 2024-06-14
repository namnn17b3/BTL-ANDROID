package com.masternam.ptitmanagestudentscore.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.StudentMainActivity;
import com.masternam.ptitmanagestudentscore.StudentViewScoreDetailActivity;
import com.masternam.ptitmanagestudentscore.adapter.StudentSubjectPointAdapter;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.dto.res.StudentViewScoreDto;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Term;
import com.masternam.ptitmanagestudentscore.service.SubjectService;
import com.masternam.ptitmanagestudentscore.service.TermService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentViewScoreFragment extends Fragment {

    TextInputLayout chooseTermInputLayout;
    AutoCompleteTextView termFieldsAutoCompleteTextView;
    List<String> termFieldItems = new ArrayList<>();
    List<Term> termList = new ArrayList<>();
    ArrayAdapter<String> termFieldAdapter = null;
    CustomProgressDialog customProgressDialog = null;
    String sessionId = null;
    int termId = 0;
    Intent intent;
    Gson gson = new Gson();
    List<StudentViewScoreDto> studentViewScoreDtos = new ArrayList<>();

    RecyclerView rcvListSubjectPoint;
    StudentSubjectPointAdapter studentSubjectPointAdapter;

    Student student;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_view_score, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", StudentViewScoreFragment.this.getContext());
        customProgressDialog = new CustomProgressDialog(this.getContext());

        chooseTermInputLayout = view.findViewById(R.id.choose_term);
        termFieldsAutoCompleteTextView = view.findViewById(R.id.term_field);
        termFieldAdapter = new ArrayAdapter<>(getContext(), R.layout.selection_item, termFieldItems);
        termFieldsAutoCompleteTextView.setAdapter(termFieldAdapter);

        rcvListSubjectPoint = view.findViewById(R.id.list_subject_point);
        studentSubjectPointAdapter = new StudentSubjectPointAdapter(this.getContext(), studentViewScoreDtos);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        rcvListSubjectPoint.setLayoutManager(linearLayoutManager);
        rcvListSubjectPoint.setAdapter(studentSubjectPointAdapter);
        studentSubjectPointAdapter.setStudentSubjectPointItemListener(new StudentSubjectPointAdapter.StudentSubjectPointItemListener() {
            @Override
            public void onStudentSubjectPointItemClick(View view, int position) {
                String studentViewScoreDtoJson = gson.toJson(studentViewScoreDtos.get(position));
                intent = new Intent(StudentViewScoreFragment.this.getContext(), StudentViewScoreDetailActivity.class);
                intent.putExtra("studentViewScoreDtoJson", studentViewScoreDtoJson);

                Student student = ((StudentMainActivity) StudentViewScoreFragment.this.getContext()).getStudent();
                String studentJson = gson.toJson(student);
                intent.putExtra("studentJson", studentJson);

                StudentViewScoreFragment.this.getContext().startActivity(intent);
            }
        });

        termFieldsAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(StudentViewScoreFragment.this.getContext(), item, Toast.LENGTH_SHORT).show();

                Term term = termList.get(position);
                termId = term.getId();

                getListStudentSubjectPoint();
            }
        });

        getStudentTerms();
    }

    private void getStudentTerms() {
        studentViewScoreDtos.clear();
        studentSubjectPointAdapter.notifyDataSetChanged();

        customProgressDialog.show();
        TermService.termService.getStudentTerms("JSESSIONID="+sessionId).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> tmp = (List<Object>) responseCommonDto.getData();
                    termFieldItems.clear();
                    termList.clear();
                    for (Object item : tmp) {
                        Term term = gson.fromJson(gson.toJson(item), Term.class);
                        System.out.println(">>> term: "+term);
                        termList.add(term);
                        String termLabel = term.getName()+"   "+AppUtils.stringifyFromStringDate(term.getStartDate())+" đến "+AppUtils.stringifyFromStringDate(term.getEndDate());
                        termFieldItems.add(termLabel);
                    }
                    termFieldAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void getListStudentSubjectPoint() {
        customProgressDialog.show();
        SubjectService.subjectService.getSubjectAndPoint("JSESSIONID="+sessionId, termId+"").enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> tmp = (List<Object>) responseCommonDto.getData();
                    studentViewScoreDtos.clear();
                    for (Object item : tmp) {
                        StudentViewScoreDto studentViewScoreDto = gson.fromJson(gson.toJson(item), StudentViewScoreDto.class);
                        studentViewScoreDtos.add(studentViewScoreDto);
                    }
                    studentSubjectPointAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) StudentViewScoreFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getListStudentSubjectPoint();
    }
}
