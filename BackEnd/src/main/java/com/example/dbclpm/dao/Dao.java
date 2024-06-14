package com.example.dbclpm.dao;

import java.sql.Connection;
import java.util.List;

import com.example.dbclpm.model.Teacher;
import com.example.dbclpm.model.Term;
import com.example.dbclpm.dto.StatisticalStudentDto;
import com.example.dbclpm.dto.StudentPointTableDtos;
import com.example.dbclpm.dto.StudentViewScoreDto;
import com.example.dbclpm.dto.TermAndSubjectsDto;
import com.example.dbclpm.model.Clazz;
import com.example.dbclpm.model.Point;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Subject;

public interface Dao {
    public Teacher getTeacherByEmail(String email);
    public List<Term> getTermsByTeacherId(int teacherId, int lt);
    public List<Subject> getSubjectsByTeacherIdAndTermId(int teacherId, int termId);
    public List<Clazz> getClazzsByTeacherIdAndTermIdAndSubjectId(int teacherId, int termId, int subjectId);
    public int getQuantityStudentPointTableDtos(int classId, String qText);
    public StudentPointTableDtos getStudentPointTableDtos(int classId, String qText, int page, int itemInPage);
    public Subject getSubjectById(int subjectId);
    public void savePoint(int pointId, Float cc, Float btl, Float th, Float ktgk, Float ktck);
    public Subject getSubjectByClassId(int classId);
    public Subject getSubjectByPointId(int pointId);
    public List<StatisticalStudentDto> getStatisticalStudentDtos(int termId, int subjectId);
    public Term getTermByPointId(int pointId);
    public Term getTermById(int termId);
    public Clazz getClassByPointId(int pointId);
    public Term getTermByClassId(int classId);
    public Teacher getTeacherByClassId(int classId);
    public Student getStudentById(int studentId);
    public void savePointByClassIdAndStudentId(Connection conn, int classId, int studentId, Float cc, Float btl, Float th, Float ktgk, Float ktck) throws Exception;
    public Point getPointByCLassIdAndStudentId(int classId, int studentId);
    public void saveTeacherByEmail(Teacher teacher);
    public Student getStudentByEmail(String email);
    public List<StudentViewScoreDto> getStudentViewScoreDtos(int studentId, int classId);
    public List<Term> getTermByStudentId(int studentId);
    public Student getStudentByPointId(int pointId);
    public List<TermAndSubjectsDto> getTermAndSubjectsByStudentId(int studentId);
    public void saveStudentByEmail(Student student);
}
