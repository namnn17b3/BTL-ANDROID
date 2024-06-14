package com.masternam.ptitmanagestudentscore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masternam.ptitmanagestudentscore.R;
import com.masternam.ptitmanagestudentscore.dto.res.PointExtent;
import com.masternam.ptitmanagestudentscore.dto.res.StudentViewScoreDto;
import com.masternam.ptitmanagestudentscore.model.Subject;

import java.util.List;

public class StudentSubjectPointAdapter extends RecyclerView.Adapter<StudentSubjectPointAdapter.StudentSubjectPointHolder> {
    private List<StudentViewScoreDto> studentViewScoreDtos;

    private Context context;

    private StudentSubjectPointItemListener listener;

    public StudentSubjectPointAdapter(Context context, List<StudentViewScoreDto> studentViewScoreDtos) {
        this.context = context;
        this.studentViewScoreDtos = studentViewScoreDtos;
    }

    @NonNull
    @Override
    public StudentSubjectPointAdapter.StudentSubjectPointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.point_item, parent, false);
        return new StudentSubjectPointAdapter.StudentSubjectPointHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentSubjectPointHolder holder, int position) {
        StudentViewScoreDto studentViewScoreDto = studentViewScoreDtos.get(position);
        if (studentViewScoreDto == null) {
            return;
        }
        Subject subject = studentViewScoreDto.getSubject();
        PointExtent pointExtent = studentViewScoreDto.getPointExtent();

        holder.pointSttTv.setText(position+1+"");
        holder.subjectNameTv.setText(subject.getName()+" ("+subject.getNumberOfCredits()+"TC)");
        if (pointExtent == null || pointExtent.getScoreByWord() == null) {
            holder.scoreByWordTv.setText("--");
            holder.scoreByWordTv.setTextColor(context.getResources().getColor(R.color.primary));
        }
        else {
            holder.scoreByWordTv.setText(pointExtent.getScoreByWord());
            if (pointExtent.getScoreByNumber() < 4.0) {
                holder.scoreByWordTv.setTextColor(context.getResources().getColor(R.color.danger));
            } else if (pointExtent.getScoreByNumber() >= 4.0 && pointExtent.getScoreByNumber() < 7) {
                holder.scoreByWordTv.setTextColor(context.getResources().getColor(R.color.warning));
            } else if (pointExtent.getScoreByNumber() >= 7 && pointExtent.getScoreByNumber() <= 10) {
                holder.scoreByWordTv.setTextColor(context.getResources().getColor(R.color.success));
            }
        }
    }

    @Override
    public int getItemCount() {
        return studentViewScoreDtos.size();
    }

    public class StudentSubjectPointHolder extends RecyclerView.ViewHolder {
        TextView pointSttTv, subjectNameTv, scoreByWordTv;
        public StudentSubjectPointHolder(@NonNull View itemView) {
            super(itemView);

            pointSttTv = itemView.findViewById(R.id.point_stt);
            subjectNameTv = itemView.findViewById(R.id.subject_name);
            scoreByWordTv = itemView.findViewById(R.id.score_by_word);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStudentSubjectPointItemClick(v, getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setStudentSubjectPointItemListener(StudentSubjectPointItemListener listener) {
        this.listener = listener;
    }

    public interface StudentSubjectPointItemListener {
        void onStudentSubjectPointItemClick(View view, int position);
    }
}
