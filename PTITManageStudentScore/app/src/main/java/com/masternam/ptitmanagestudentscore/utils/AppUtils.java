package com.masternam.ptitmanagestudentscore.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
//import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.constant.AppConstant;
import com.masternam.ptitmanagestudentscore.dto.res.ResponseCommonDto;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.model.Teacher;
import com.masternam.ptitmanagestudentscore.service.AuthenService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class AppUtils {
    public static void show(Activity activity, int constrain, int dialog, int messageId, int btnClose, String message) {
        ConstraintLayout constraintLayout = activity.findViewById(constrain);
        View view = LayoutInflater.from(activity).inflate(dialog, constraintLayout);
        Button btn = view.findViewById(btnClose);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        TextView errorDesc = view.findViewById(messageId);
        errorDesc.setText(message);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    public static String getCookie(String cookies, String key) {
        Map<String, String> map = new HashMap<>();
        String[] arr = cookies.split("; ");
        for (String s : arr) {
            String[] childArr = s.split("=");
            if (childArr.length == 2) {
                map.put(childArr[0], childArr[1]);
            }
        }
        return map.get(key);
    }

    public static void saveDataToSharedPreferences(String key, String value, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getDataToSharedPreferences(String key, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void removeDataToSharedPreferences(String key, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("key");
        editor.apply();
    }

    public static Teacher getTeacherInfoSync(Context context) {
        Gson gson = new Gson();
        Teacher teacher = null;
        try {
            String jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", context);
            System.out.println(">>> JSESSIONID sync: " + jsessionId);
            Response<ResponseCommonDto> response = AuthenService.authenService.teacherInfo("JSESSIONID="+jsessionId).execute();
            if (!response.isSuccessful()) {
                return teacher;
            }
            ResponseCommonDto responseCommonDto = response.body();
            teacher = gson.fromJson(gson.toJson(responseCommonDto.getData()), Teacher.class);
            System.out.println(">>> teacher from sync fn: " + teacher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teacher;
    }

    public static Student getStudentInfoSync(Context context) {
        Gson gson = new Gson();
        Student student = null;
        try {
            String jsessionId = AppUtils.getDataToSharedPreferences("JSESSIONID", context);
            System.out.println(">>> JSESSIONID sync: " + jsessionId);
            Response<ResponseCommonDto> response = AuthenService.authenService.studentInfo("JSESSIONID="+jsessionId).execute();
            if (!response.isSuccessful()) {
                return student;
            }
            ResponseCommonDto responseCommonDto = response.body();
            student = gson.fromJson(gson.toJson(responseCommonDto.getData()), Student.class);
            System.out.println(">>> teacher from sync fn: " + student);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return student;
    }

//    public static ProgressBar makeProgressBar(Context context) {
//        ProgressBar progressBar = new ProgressBar(context);
//
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
//                context.getResources().getDimensionPixelSize(R.dimen.progress_bar_width),
//                context.getResources().getDimensionPixelSize(R.dimen.progress_bar_height));
//        progressBar.setLayoutParams(layoutParams);
//
//        int color = context.getResources().getColor(R.color.login_title);
//        progressBar.setIndeterminateTintList(ColorStateList.valueOf(color));
//
//        Drawable drawable = context.getResources().getDrawable(R.drawable.progress_bg);
//        progressBar.setBackground(drawable);
//
//        return progressBar;
//    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String stringifyFromStringDate(String s) {
        String[] monthStrings = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] arr = s.replaceAll(",", "").split("\\s+");
        String month = "";
        for (int i = 0; i < monthStrings.length; i++) {
            if (monthStrings[i].equals(arr[0])) {
                if (i + 1 > 9) {
                    month = String.valueOf(i + 1);
                } else {
                    month = "0"+String.valueOf(i + 1);
                }
                break;
            }
        }
        String day = arr[1].length() == 1 ? "0"+arr[1] : arr[1];
        String year = arr[2];
        return year+"-"+month+"-"+day;
    }
}
