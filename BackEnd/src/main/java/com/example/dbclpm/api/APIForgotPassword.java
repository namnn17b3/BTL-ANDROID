package com.example.dbclpm.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Teacher;
import com.example.dbclpm.utils.RandomCode;
import com.example.dbclpm.utils.SendEmail;
import com.google.gson.Gson;

@WebServlet("/api/teacher/forgot-password")
public class APIForgotPassword extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		
		String email = req.getParameter("email");
		for (String value : APIResetPassword.map.values()) {
			if (email.equals(value)) {
				resp.setStatus(400);
				pw.println(gson.toJson(new ResponseCommonDto("Mã code đã được gửi ở lần trước đó, vui lòng kiểm tra email", 400, null)));
				return;
			}
		}
		
		Teacher teacher = dao.getTeacherByEmail(email);
		Student student = dao.getStudentByEmail(email);
		
		if (teacher == null && student == null) {
			resp.setStatus(400);
			pw.println(gson.toJson(new ResponseCommonDto("Email không tồn tại", 400, null)));
			return;
		}
		
		String code = RandomCode.randomCode();
		// Nội dung
		String content = 
		"<h3>PTIT Manage Score --- Mã code đặt lại mật khẩu</h3>"
		+"<div>Mã code: "+code+"</div>"
		+ "<div>Dùng mã này để đặt lại mật khẩu</div>"
		+"<div style='color: red'><i>Chú ý: Đây là mail tự động! Vui lòng không reply!</i></div>";
		
		SendEmail.sendMail(email, content);
		
		APIResetPassword.map.put(code, email);
		pw.println(gson.toJson(new ResponseCommonDto("OK", 200, null)));
	}
}
