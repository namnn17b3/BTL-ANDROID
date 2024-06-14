package com.masternam.ptitmanagestudentscore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.StudentProfileActivity;
import com.masternam.ptitmanagestudentscore.model.Student;
import com.masternam.ptitmanagestudentscore.utils.AppUtils;

import java.util.List;

public class StudentListApdapter extends RecyclerView.Adapter<StudentListApdapter.StudentListHolder> {
    private List<Student> listStudent;

    private int classId = 0;

    private int page = 1;

    public List<Student> getListStudent() {
        return this.listStudent;
    }

    private StudentItemListener listener;

    private Context context;

    private Gson gson = new Gson();

    public void setStudentItemListener(StudentItemListener listener) {
        this.listener = listener;
    }

    public StudentListApdapter(Context context, List<Student> listStudent, int classId, int page) {
        this.listStudent = listStudent;
        this.classId = classId;
        this.page = page;
        this.context = context;
    }

    @NonNull
    @Override
    public StudentListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new StudentListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentListHolder holder, @SuppressLint("RecyclerView") int position) {
        Student student = listStudent.get(position);
        if (student == null) {
            return;
        }
        try {
            holder.stNameTv.setText(student.getName());
            holder.stMsvTv.setText("SV"+student.getId());
            holder.stDobTv.setText(AppUtils.stringifyFromStringDate(student.getDateOfBirth()));
            holder.stClassTv.setText(student.getAdministrativeClass());
            holder.stSttTv.setText((position + 1)+"");
            if (student.getGender().equals("Nam")) {
                holder.genderImg.setImageResource(R.drawable.baseline_male_24);
            } else {
                holder.genderImg.setImageResource(R.drawable.baseline_female_24);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listStudent.size();
    }

    public class StudentListHolder extends RecyclerView.ViewHolder {
        TextView stNameTv, stMsvTv, stDobTv, stClassTv, stSttTv;
        ImageView genderImg;
        Button btnStudentInfoMenu;
        public StudentListHolder(@NonNull View itemView) {
            super(itemView);
            stNameTv = itemView.findViewById(R.id.st_name);
            stMsvTv = itemView.findViewById(R.id.st_msv);
            stDobTv = itemView.findViewById(R.id.st_dob);
            stClassTv = itemView.findViewById(R.id.st_class);
            stSttTv = itemView.findViewById(R.id.st_stt);
            genderImg = itemView.findViewById(R.id.gender_img);
            btnStudentInfoMenu = itemView.findViewById(R.id.student_info_menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStudentItemClick(v, getAdapterPosition());
                    }
                }
            });

            btnStudentInfoMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupStudentInfoMenu(v);
                }
            });
        }

        private void showPopupStudentInfoMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.pop_up_student_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    handleMenuItemClick(item);
                    return false;
                }
            });
            popupMenu.show();
        }

        private void handleMenuItemClick(MenuItem item) {
            Student student = listStudent.get(getAdapterPosition());
            Intent intent = new Intent(context, StudentProfileActivity.class);
            intent.putExtra("studentJson", gson.toJson(student));
            context.startActivity(intent);
        }
    }

    public interface StudentItemListener {
        void onStudentItemClick(View view, int position);
    }
}
