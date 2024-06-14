package com.example.dbclpm.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Teacher;
import com.google.gson.Gson;

@WebServlet("/api/change-password")
public class APIChangePassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	PrintWriter pw = resp.getWriter();
    	HttpSession session = req.getSession();
    	
    	Teacher teacher = (Teacher) session.getAttribute("teacher");
    	Student student = (Student) session.getAttribute("session");
    	
    	String oldPassword = req.getParameter("oldPassword");
    	String newPassword = req.getParameter("newPassword");
    	String confirmNewPassword = req.getParameter("confirmNewPassword");
    	
    	if (teacher != null && !BCrypt.checkpw(oldPassword, teacher.getPassword())) {
    		resp.setStatus(400);
    		pw.println(gson.toJson(new ResponseCommonDto("Mật khẩu cũ không khớp", 400, null)));
    		return;
    	}
    	
    	if (student != null && !BCrypt.checkpw(oldPassword, student.getPassword())) {
    		resp.setStatus(400);
    		pw.println(gson.toJson(new ResponseCommonDto("Mật khẩu cũ không khớp", 400, null)));
    		return;
    	}
    	
    	if (newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=.*[0-9]).{8,}$") == false) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Mật mới khẩu phải có ít nhất 8 kí tự, có chứa kí tự hoa, thường, số, đặc biệt", 400, null)));
			return;
		}
    	
		if (confirmNewPassword.equals(newPassword) == false) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Xác nhận mật khẩu mới không khớp", 400, null)));
			return;
		}
		
		if (teacher != null) {
			teacher.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
			dao.saveTeacherByEmail(teacher);
		} else {
			student.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
			dao.saveStudentByEmail(student);
		}
		
		pw.println(gson.toJson(new ResponseCommonDto("OK", 200, null)));
    }
}
