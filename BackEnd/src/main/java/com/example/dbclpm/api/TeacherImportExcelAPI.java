package com.example.dbclpm.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dao.impl.PoolConnection;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.exception.AppException;
import com.example.dbclpm.model.Point;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Subject;
import com.example.dbclpm.model.Teacher;
import com.example.dbclpm.model.Term;
import com.example.dbclpm.utils.MqttHandler;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;

@WebServlet(urlPatterns={"/api/teacher/import-excel"}, loadOnStartup=1)
@MultipartConfig
(
    maxFileSize = 10 * 1024 * 1024, // 10MB
    fileSizeThreshold = 5 * 1024 * 1024, // 5MB
    maxRequestSize = 20 * 1024 * 1024 // 20MB
)
public class TeacherImportExcelAPI extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();
    private static final DataFormatter dataFormatter = new DataFormatter();
    private static HikariDataSource poolConnection = PoolConnection.getPoolConnection();
    private static final BigDecimal zero = new BigDecimal("0");
    private static final BigDecimal ten = new BigDecimal("10");
    private static final MqttHandler mqttHandler = new MqttHandler("server-import");
    
    private Object getValue(Row row, int index, HttpServletResponse resp, String tag, int percent) throws Exception {
        String value = dataFormatter.formatCellValue(row.getCell(index));
        
        if (tag.equals("int")) {
            return Integer.parseInt(value.substring(2));
        }
        else if (tag.equals("BigDecimal") && percent > 0) {
            BigDecimal x = new BigDecimal(value);
            if (x.compareTo(zero) < 0) {
                throw new AppException("Điểm không được nhỏ hơn 0", 400);
            }
            if (x.compareTo(ten) > 0) {
                throw new AppException("Điểm không được lớn hơn 10", 400);
            }
            return x;
        } else if (tag.equals("BigDecimal") && percent == 0) {
            return null;
        }
        return null;
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter pw = resp.getWriter();
        HttpSession session = req.getSession();
        Teacher currentTeacher = (Teacher) session.getAttribute("teacher");
        
        Part filePart = null;
        try {
        	filePart = req.getPart("excelFile");
        } catch (Exception e) {
        	resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("File size exceeds the limit (10MB)", 400, null)));
            return;
		}
        
        if (filePart == null) {
        	resp.setStatus(400);
        	pw.println(gson.toJson(new ResponseCommonDto("Required file excelFile", 400, null)));
        	return;
        }
        
        long fileSize = filePart.getSize();
        if (fileSize > 10 * 1024 * 1024) {
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("File size exceeds the limit (10MB)", 400, null)));
            return;
        }
        
        InputStream inputStream = filePart.getInputStream();
        byte[] headerBytes = new byte[4];
        inputStream.read(headerBytes, 0, 4);
        inputStream.close();
        String headerHex = bytesToHex(headerBytes);
        
        if (!"504B0304".equals(headerHex.toUpperCase())) {
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("Only accept excel file", 400, null)));
            return;
        }
        
        Workbook workbook = WorkbookFactory.create(filePart.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        
        int classId = 0;
        try {
            classId = Integer.parseInt(req.getParameter("classId"));
        } catch (Exception ex) {
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("classId invalid integer", 400, null)));
            return;
        }
        
        Term term = dao.getTermByClassId(classId);
        long now = System.currentTimeMillis();
        if (term.getEndDate().getTime() < now|| term.getStartDate().getTime() > now) {
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("Term is not match", 400, null)));
            return;
        }
        
        Teacher teacher = dao.getTeacherByClassId(classId);
        if (currentTeacher.getId() != teacher.getId()) {
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("Teacher is not match", 400, null)));
            return;
        }
        
        Subject subject = dao.getSubjectByClassId(classId);
        List<String> topics = new ArrayList<>();
        
        int rowIndex = 0;
        Connection conn = null;
        boolean isError = false;
        try {
            conn = poolConnection.getConnection();
            conn.setAutoCommit(false);
            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
                Integer studentId = (Integer) getValue(row, 0, resp, "int", 0);
                Student student = dao.getStudentById(studentId);
                Point point = dao.getPointByCLassIdAndStudentId(classId, studentId);
                if (point == null) {
                    throw new AppException("Student not in current class "+" CN"+classId, 400);
                }
                
                topics.add(term.getId()+"_"+subject.getId()+"_"+student.getId());
                
                BigDecimal cc = (BigDecimal) getValue(row, 1, resp, "BigDecimal", subject.getPercentCC());
                BigDecimal btl = (BigDecimal) getValue(row, 2, resp, "BigDecimal", subject.getPercentBTL());
                BigDecimal th = (BigDecimal) getValue(row, 3, resp, "BigDecimal", subject.getPercentTH());
                BigDecimal ktgk = (BigDecimal) getValue(row, 4, resp, "BigDecimal", subject.getPercentKTGK());
                BigDecimal ktck = (BigDecimal) getValue(row, 5, resp, "BigDecimal", subject.getPercentCC());
                
                dao.savePointByClassIdAndStudentId(
                    conn,
                    classId,
                    student.getId(),
                    cc != null && subject.getPercentCC() > 0 ? cc.floatValue() : null,
                    btl != null && subject.getPercentBTL() > 0 ? btl.floatValue() : null,
                    th != null && subject.getPercentTH() > 0 ? th.floatValue() : null,
                    ktgk != null && subject.getPercentKTGK() > 0 ? ktgk.floatValue() : null,
                    ktck != null && subject.getPercentKTCK() > 0 ? ktck.floatValue() : null
                );
            }
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
                System.out.println("Transaction Rollback");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            isError = true;
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto(e.getMessage(), 400, null)));
        } finally {
            workbook.close();
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isError) {
            resp.setStatus(200);
            pw.println(gson.toJson(new ResponseCommonDto("OK", 200, null)));
        }
        
        if (MqttHandler.SEND_NOTI) {
        	for (String topic : topics) {
        		mqttHandler.publish(topic, "Đã có điểm môn học "+subject.getName()+" học kì "+term.getName());
        	}
        }
    }
}
