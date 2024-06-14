package com.masternam.ptitmanagestudentscore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.adapter.StudentListApdapter;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.custom.CustomProgressDialog;
import com.masternam.ptitmanagestudentscore.dto.res.PointExtent;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.dto.res.StudentPointTable;
import com.masternam.ptitmanagestudentscore.dto.res.StudentPointTableDtos;
import com.masternam.ptitmanagestudentscore.listener.PaginationScrollListener;
import com.masternam.ptitmanagestudentscore.model.Point;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.service.PointService;
import com.masternam.ptitmanagestudentscore.service.StudentService;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;
import com.masternam.ptitmanagestudentscore.utils.RealPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;

public class StudentListActivity extends AppCompatActivity {

    private Button btnBack;
    private RecyclerView rcvListStudent;
    private StudentListApdapter studentListApdapter;
    private List<Student> listStudent = new ArrayList<>();
    private List<Point> listPoint = new ArrayList<>();
    private List<PointExtent> listPointExtent = new ArrayList<>();
    private boolean isLoading = false, isLastPage = false;
    private int currentPage = 1;
    private int totalPage = 0;
    private Intent intent = null;
    private String jsessionId = null;
    private Gson gson = new Gson();
    private CustomProgressDialog customProgressDialog;
    private Button btnMenu;
    private static final String TAG = StudentListActivity.class.getName();
    private static final int MY_REQUEST_CODE = 100;
    private int classId;

    private String qText = null;

    private LinearLayout wapperSearchbar;

    private Button btnSearch, btnCancel;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", StudentListActivity.this);
        customProgressDialog = new CustomProgressDialog(StudentListActivity.this);
        btnMenu = findViewById(R.id.menu);
        btnBack = findViewById(R.id.btn_back);

        wapperSearchbar = findViewById(R.id.wapper_search_bar);
        searchView = findViewById(R.id.search_bar);
        btnSearch = findViewById(R.id.btn_seacrh);
        btnCancel = findViewById(R.id.btn_cancel);

        // lay data classId tu intent gui sang
        intent = getIntent();
        classId = intent.getIntExtra("classId", 0);
        String subjectJson = intent.getStringExtra("subjectJson");
        System.out.println(">>> classId: "+classId);

        rcvListStudent = findViewById(R.id.list_student);
        studentListApdapter = new StudentListApdapter(StudentListActivity.this, listStudent, classId, currentPage);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvListStudent.setLayoutManager(linearLayoutManager);
        rcvListStudent.setAdapter(studentListApdapter);
        studentListApdapter.setStudentItemListener(new StudentListApdapter.StudentItemListener() {
            @Override
            public void onStudentItemClick(View view, int position) {
                intent = new Intent(StudentListActivity.this, EditPointActivity.class);
                intent.putExtra("subjectJson", subjectJson);
                intent.putExtra("studentJson", gson.toJson(listStudent.get(position)));
                intent.putExtra("pointJson", gson.toJson(listPoint.get(position)));
                intent.putExtra("pointExtentJson", gson.toJson(listPointExtent.get(position)));
                StudentListActivity.this.startActivity(intent);
            }
        });

//        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        rcvListStudent.addItemDecoration(itemDecoration);

        rcvListStudent.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            public void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                System.out.println(">>> currentPage: " + currentPage);
                getStudents(classId, qText, currentPage);
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentListActivity.this.finish();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPagination();
                qText = searchView.getQuery()+"";
                System.out.println(">>> qText: "+qText);
                getStudents(classId, qText, 1);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPagination();
                wapperSearchbar.setVisibility(View.GONE);
                getStudents(classId, qText, 1 );
            }
        });

        registerForContextMenu(btnMenu);
        getStudents(classId, null,1);
    }

    private void getStudents(int classId, String qText, int page) {
        customProgressDialog.show();
        StudentService.studentService.getStudents("JSESSIONID="+jsessionId, classId+"", qText, page+"").enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                Toast.makeText(StudentListActivity.this, "Trang "+page, Toast.LENGTH_SHORT).show();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    responseCommonDto = response.body();
                    StudentPointTableDtos studentPointTableDtos = gson.fromJson(gson.toJson(responseCommonDto.getData()), StudentPointTableDtos.class);
                    List<StudentPointTable> studentPointTables = (List<StudentPointTable>) studentPointTableDtos.getStudentPointTables();
                    System.out.println(">>> page: " + page);
                    for (StudentPointTable item : studentPointTables) {
//                        Student student = gson.fromJson(gson.toJson(item), Student.class);
                        Student student = item.getStudent();
                        Point point = item.getPoint();
                        PointExtent pointExtent = item.getPointExtent();

                        System.out.println(">>> student: "+student);
                        System.out.println(">>> point: "+point);
                        System.out.println(">>> pointExtent: "+pointExtent);

                        listStudent.add(student);
                        listPoint.add(point);
                        listPointExtent.add(pointExtent);
                    }
                    studentListApdapter.notifyDataSetChanged();
                    totalPage = studentPointTableDtos.getQuantity() % AppConstant.ITEM_IN_PAGE == 0
                            ? studentPointTableDtos.getQuantity() / AppConstant.ITEM_IN_PAGE
                            : studentPointTableDtos.getQuantity() / AppConstant.ITEM_IN_PAGE + 1;

                    isLoading = false;
                    if (page == totalPage) {
                        isLastPage = true;
                    }
                } catch (Exception e) {
                    customProgressDialog.close();
                    AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Tìm kiếm sinh viên");
        menu.add(0, v.getId(), 0, "Import Excel");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Tìm kiếm sinh viên")) {
            // call api tim kiem sinh vien
            wapperSearchbar.setVisibility(View.VISIBLE);
        } else {
            // call api import excel
            onClickRequestPermission();
        }
        return true;
    }

    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openFolder();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openFolder();
        } else {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission, MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFolder();
            }
        }
    }

    private void openFolder() {
        intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Chọn File"));
    }

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.e(TAG, "onActivityResult");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    uploadFile(result);
                }
            }
        }
    );

    private void uploadFile(ActivityResult result) {
        Intent data = result.getData();
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        String realPath = RealPathUtil.getRealPath(StudentListActivity.this, uri);
        File file = new File(realPath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multiPartBody = MultipartBody.Part.createFormData("excelFile", file.getName(), requestBody);
        requestToServer(multiPartBody);
    }

    private void requestToServer(MultipartBody.Part multiPartBody) {
        customProgressDialog.show();
        PointService.pointService.uploadFile(
                "JSESSIONID="+jsessionId,
                classId+"",
                multiPartBody).enqueue(new Callback<ResponseCommonDto>() {
            @Override
            public void onResponse(Call<ResponseCommonDto> call, Response<ResponseCommonDto> response) {
                customProgressDialog.close();
                try {
                    ResponseCommonDto responseCommonDto;
                    if (!response.isSuccessful()) {
                        String json = response.errorBody().string();
                        responseCommonDto = gson.fromJson(json, ResponseCommonDto.class);
                        AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, responseCommonDto.getMessage());
                        return;
                    }
                    AppUtils.show(StudentListActivity.this, R.id.successConstrainLayout, R.layout.success_dialog, R.id.successDesc, R.id.successDone, "Cập nhật điểm thành công");
                    currentPage = 1;
                } catch (Exception e) {
                    AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                }
            }

            @Override
            public void onFailure(Call<ResponseCommonDto> call, Throwable t) {
                customProgressDialog.close();
                if (t.getMessage().contains("NO_ERROR")) {
                    AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Kích thước file quá 10MB");
                } else {
                    AppUtils.show(StudentListActivity.this, R.id.errorConstrainLayout, R.layout.error_dialog, R.id.errorDesc, R.id.errorDone, "Đã có lỗi xảy ra vui lòng thử lại sau!");
                }
            }
        });
    }

    private void resetPagination() {
        listStudent.clear();
        listPoint.clear();
        listPointExtent.clear();
        totalPage = 0;
        currentPage = 1;
        isLoading = false;
        isLastPage = false;
        qText = null;
    }
}