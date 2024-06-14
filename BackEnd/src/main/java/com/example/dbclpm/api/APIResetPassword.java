package com.example.dbclpm.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Teacher;
import com.google.gson.Gson;

@WebServlet(urlPatterns={"/api/teacher/reset-password"}, loadOnStartup=1)
public class APIResetPassword extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();
    
    public static Map<String, String> map = new HashMap<>();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		String password = req.getParameter("password");
		String confirmPassword = req.getParameter("confirmPassword");
		String code = req.getParameter("code");
		
		String email = map.get(code);
		if (email == null) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Mã code không chính xác hoặc đã hết hiệu lực", 400, null)));
			return;
		}
		
		if (password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=.*[0-9]).{8,}$") == false) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Mật khẩu phải có ít nhất 8 kí tự, có chứa kí tự hoa, thường, số, đặc biệt", 400, null)));
			return;
		}
		
		if (password.equals(confirmPassword) == false) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Xác nhận mật khẩu không khớp", 400, null)));
			return;
		}
		
		Teacher teacher = dao.getTeacherByEmail(email);
		if (teacher != null) {
			teacher.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
			dao.saveTeacherByEmail(teacher);
		} else {
			Student student = dao.getStudentByEmail(email);
			student.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
			dao.saveStudentByEmail(student);
		}
		
		map.remove(code);
		pw.println(gson.toJson(new ResponseCommonDto("OK", 200, null)));
	}
}
