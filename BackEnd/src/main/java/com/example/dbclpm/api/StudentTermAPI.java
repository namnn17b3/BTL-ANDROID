package com.example.dbclpm.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Term;
import com.google.gson.Gson;

@WebServlet("/api/student/term")
public class StudentTermAPI extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	PrintWriter pw = resp.getWriter();
    	HttpSession session = req.getSession();
    	
    	Student student = (Student) session.getAttribute("student");
    	List<Term> list = dao.getTermByStudentId(student.getId());
    	
    	pw.println(gson.toJson(new ResponseCommonDto("OK", 200, list)));
    }
}
