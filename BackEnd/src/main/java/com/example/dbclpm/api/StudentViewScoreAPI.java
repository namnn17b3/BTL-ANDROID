package com.example.dbclpm.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dao.impl.DaoImpl;
import com.example.dbclpm.dto.PointExtent;
import com.example.dbclpm.dto.ResponseCommonDto;
import com.example.dbclpm.dto.StudentViewScoreDto;
import com.example.dbclpm.model.Point;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Subject;
import com.example.dbclpm.utils.PointUtils;
import com.google.gson.Gson;

@WebServlet("/api/student/view-score")
public class StudentViewScoreAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Dao dao = new DaoImpl();
    private static final Gson gson = new Gson();
    private static final BigDecimal zero = new BigDecimal("0");
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	PrintWriter pw = resp.getWriter();
    	int termId = 0;
        
    	try {
            termId = Integer.parseInt(req.getParameter("termId").trim());
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.setStatus(400);
            pw.println(gson.toJson(new ResponseCommonDto("Error Format Integer For termId", 400, null)));
            return;
        }
        
    	HttpSession session = req.getSession();
    	Student student = (Student) session.getAttribute("student");
    	
    	List<StudentViewScoreDto> list = dao.getStudentViewScoreDtos(student.getId(), termId);
    	for (StudentViewScoreDto studentViewScoreDto : list) {
    		Subject subject = studentViewScoreDto.getSubject();
    		Point point = studentViewScoreDto.getPoint();
    		
    		String note = null;
    		String scoreByWord = null;
    		Float scorePerFourRank = null;
    		Float scoreByNumber = null;    		
    		
    		if (
				PointUtils.checkPointValid(point.getCc(), subject.getPercentCC()) &&
				PointUtils.checkPointValid(point.getBtl(), subject.getPercentBTL()) &&
				PointUtils.checkPointValid(point.getTh(), subject.getPercentTH()) &&
				PointUtils.checkPointValid(point.getKtgk(), subject.getPercentKTGK()) &&
				PointUtils.checkPointValid(point.getKtck(), subject.getPercentKTCK())
			) {
	    		BigDecimal averagePoint = null;
	    		BigDecimal cc = subject.getPercentCC() > 0 ? new BigDecimal(point.getCc()) : null;
	    		BigDecimal btl = subject.getPercentBTL() > 0 ? new BigDecimal(point.getBtl()): null;
	    		BigDecimal th = subject.getPercentTH() > 0 ? new BigDecimal(point.getTh()) : null;
	    		BigDecimal ktgk = subject.getPercentKTGK() > 0 ? new BigDecimal(point.getKtgk()) : null;
	    		BigDecimal ktck = subject.getPercentKTCK() > 0 ? new BigDecimal(point.getKtck()) : null;
	    		
	    		averagePoint = PointUtils.calcAveragePoint(
					subject,
		            cc != null && subject.getPercentCC() > 0 ? cc : zero,
		            btl != null && subject.getPercentBTL() > 0 ? btl : zero,
		            th != null && subject.getPercentTH() > 0 ? th : zero,
		            ktgk != null && subject.getPercentKTGK() > 0 ? ktgk : zero,
		            ktck != null && subject.getPercentKTCK() > 0 ? ktck : zero
		        );
		        scoreByWord = PointUtils.genScorebyWord(averagePoint);
		        scorePerFourRank = PointUtils.genScorePerFourRank(scoreByWord);
		        note = scoreByWord.equals("F") ? "Không đạt" : PointUtils.genNote(cc, btl, th, ktgk, ktck);
		        scoreByNumber = averagePoint.floatValue();
    		}
	        
	        PointExtent pointExtent = new PointExtent(scoreByNumber, scoreByWord, scorePerFourRank, note);
	        studentViewScoreDto.setPointExtent(pointExtent);
    	}
    	pw.println(gson.toJson(new ResponseCommonDto("OK", 200, list)));
    }
}
