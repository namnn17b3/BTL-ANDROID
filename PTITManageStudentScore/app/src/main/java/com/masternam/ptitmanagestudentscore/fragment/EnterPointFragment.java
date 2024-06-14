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

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.StudentListActivity;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.model.Clazz;
import com.masternam.ptitmanagestudentscore.model.Term;
import com.masternam.ptitmanagestudentscore.model.Subject;
import com.masternam.ptitmanagestudentscore.service.ClazzService;
import com.masternam.ptitmanagestudentscore.service.SubjectService;
import com.masternam.ptitmanagestudentscore.service.TermService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterPointFragment extends Fragment {
    TextInputLayout chooseTermInputLayout;
    TextInputLayout chooseSubjectInputLayout;
    TextInputLayout chooseClazzInputLayout;
    AutoCompleteTextView termFieldsAutoCompleteTextView;
    AutoCompleteTextView subjectFieldsAutoCompleteTextView;
    AutoCompleteTextView clazzFieldsAutoCompleteTextView;
    List<String> termFieldItems = new ArrayList<>();
    List<Term> termList = new ArrayList<>();

    List<String> subjectFieldItems = new ArrayList<>();
    List<Subject> subjectList = new ArrayList<>();

    List<String> clazzFieldItems = new ArrayList<>();
    List<Clazz> clazzList = new ArrayList<>();

    ArrayAdapter<String> termFieldAdapter = null;
    ArrayAdapter<String> subjectFieldAdapter = null;
    ArrayAdapter<String> clazzFieldAdapter = null;

    Gson gson = new Gson();

    CustomProgressDialog customProgressDialog = null;
    String sessionId = null;

    int termId = 0;

    Intent intent;

    Subject subjectShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_point, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", EnterPointFragment.this.getContext());

        chooseTermInputLayout = view.findViewById(R.id.choose_term);
        termFieldsAutoCompleteTextView = view.findViewById(R.id.term_field);
        termFieldAdapter = new ArrayAdapter<>(getContext(), R.layout.selection_item, termFieldItems);
        termFieldsAutoCompleteTextView.setAdapter(termFieldAdapter);

        chooseSubjectInputLayout = view.findViewById(R.id.choose_subject);
        subjectFieldsAutoCompleteTextView = view.findViewById(R.id.subject_field);
        subjectFieldAdapter = new ArrayAdapter<>(getContext(), R.layout.selection_item, subjectFieldItems);
        subjectFieldsAutoCompleteTextView.setAdapter(subjectFieldAdapter);

        chooseClazzInputLayout = view.findViewById(R.id.choose_class);
        clazzFieldsAutoCompleteTextView = view.findViewById(R.id.class_field);
        clazzFieldAdapter = new ArrayAdapter<>(getContext(), R.layout.selection_item, clazzFieldItems);
        clazzFieldsAutoCompleteTextView.setAdapter(clazzFieldAdapter);

        termFieldsAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(EnterPointFragment.this.getContext(), item, Toast.LENGTH_SHORT).show();

                Term term = termList.get(position);
                termId = term.getId();

                subjectList.clear();
                subjectFieldAdapter.notifyDataSetChanged();

                clazzList.clear();
                clazzFieldAdapter.notifyDataSetChanged();

                getSubjects(termId);
            }
        });

        subjectFieldsAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(EnterPointFragment.this.getContext(), item, Toast.LENGTH_SHORT).show();

                Subject subject = subjectList.get(position);
                subjectShare = subject;

                clazzList.clear();
                clazzFieldAdapter.notifyDataSetChanged();

                getClasses(subject.getId());
            }
        });

        clazzFieldsAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(EnterPointFragment.this.getContext(), item, Toast.LENGTH_SHORT).show();

                Clazz clazz = clazzList.get(position);
                intent = new Intent(EnterPointFragment.this.getContext(), StudentListActivity.class);
                intent.putExtra("classId", clazz.getId());
                intent.putExtra("subjectJson", gson.toJson(subjectShare));
                EnterPointFragment.this.startActivity(intent);
            }
        });

        customProgressDialog = new CustomProgressDialog(this.getContext());
        getTeacherTerms();
    }

    private void getClasses(int subjectId) {
        ClazzService.clazzService.getClasses("JSESSIONID="+sessionId, String.valueOf(termId), String.valueOf(subjectId)).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> tmp = (List<Object>) responseCommonDto.getData();
                    clazzFieldItems.clear();
                    clazzList.clear();
                    for (Object item : tmp) {
                        Clazz clazz = gson.fromJson(gson.toJson(item), Clazz.class);
                        System.out.println(">>> class: "+clazz);
                        clazzList.add(clazz);
                        clazzFieldItems.add(clazz.getName());
                    }
                    clazzFieldAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void getSubjects(int termId) {
        SubjectService.subjectService.getSubjects("JSESSIONID="+sessionId, String.valueOf(termId)).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    List<Object> tmp = (List<Object>) responseCommonDto.getData();
                    subjectFieldItems.clear();
                    subjectList.clear();
                    for (Object item : tmp) {
                        Subject subject = gson.fromJson(gson.toJson(item), Subject.class);
                        System.out.println(">>> subject: "+subject);
                        subjectList.add(subject);
                        String subjectLabel = subject.getName()+" ("+subject.getNumberOfCredits()+"TC)";
                        subjectFieldItems.add(subjectLabel);
                    }
                    subjectFieldAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    private void getTeacherTerms() {
        customProgressDialog.show();
        TermService.termService.getTeacherTerms("JSESSIONID="+sessionId, "0").enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
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
                    AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show((Activity) EnterPointFragment.this.getContext(), R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getTeacherTerms();
    }
}
