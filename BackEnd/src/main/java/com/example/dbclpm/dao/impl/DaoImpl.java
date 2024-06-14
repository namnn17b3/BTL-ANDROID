package com.example.dbclpm.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.dbclpm.dao.Dao;
import com.example.dbclpm.dto.StatisticalStudentDto;
import com.example.dbclpm.dto.StudentPointTable;
import com.example.dbclpm.dto.StudentPointTableDtos;
import com.example.dbclpm.dto.StudentViewScoreDto;
import com.example.dbclpm.dto.TermAndSubjectsDto;
import com.example.dbclpm.model.Clazz;
import com.example.dbclpm.model.Point;
import com.example.dbclpm.model.Student;
import com.example.dbclpm.model.Subject;
import com.example.dbclpm.model.Teacher;
import com.example.dbclpm.model.Term;
import com.zaxxer.hikari.HikariDataSource;

public class DaoImpl implements Dao {

	private static HikariDataSource poolConnection = PoolConnection.getPoolConnection();

	@Override
	public Teacher getTeacherByEmail(String email) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Teacher teacher = null;

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from teacher where email = binary ?");
			ppstm.setString(1, email);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				teacher = new Teacher();
				teacher.setId(rs.getInt("id"));
				teacher.setName(rs.getString("name"));
				teacher.setEmail(rs.getString("email"));
				teacher.setPassword(rs.getString("password"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return teacher;
	}

	@Override
	public List<Term> getTermsByTeacherId(int teacherId, int lt) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<Term> list = new ArrayList<>();

		try {
			conn = poolConnection.getConnection();
			String sql = "select distinct term.*\n" + "from class, term  \n"
					+ "where class.teacher_id = ? and class.term_id = term.id\n"
					+ (lt == 0 ? "and DATE(NOW()) between term.start_date and term.end_date"
							: "and term.end_date < DATE(NOW())");
			ppstm = conn.prepareStatement(sql);
			ppstm.setInt(1, teacherId);

			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				Term term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
				list.add(term);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public List<Subject> getSubjectsByTeacherIdAndTermId(int teacherId, int termId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<Subject> list = new ArrayList<>();

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select distinct subject.*\n" + "from class, subject\n"
					+ "where class.term_id = ? and class.teacher_id = ?");
			ppstm.setInt(1, teacherId);
			ppstm.setInt(2, termId);

			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				Subject subject = new Subject();
				subject.setId(rs.getInt("id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
				list.add(subject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public List<Clazz> getClazzsByTeacherIdAndTermIdAndSubjectId(int teacherId, int termId, int subjectId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<Clazz> list = new ArrayList<>();

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select class.*\n" + "from class\n" + "where class.teacher_id = ? and\n"
					+ "class.term_id = ? and class.subject_id = ?;");
			ppstm.setInt(1, teacherId);
			ppstm.setInt(2, termId);
			ppstm.setInt(3, subjectId);

			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				Clazz clazz = new Clazz();
				clazz.setId(rs.getInt("id"));
				clazz.setName(rs.getString("name"));
				clazz.setRoomName(rs.getString("room_name"));
				clazz.setTeacherId(rs.getInt("teacher_id"));
				clazz.setSubjectId(rs.getInt("subject_id"));
				clazz.setTermId(rs.getInt("term_id"));
				list.add(clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}

	@Override
    public int getQuantityStudentPointTableDtos(int classId, String qText) {
        Connection conn = null;
        PreparedStatement ppstm = null;
        int quantity = 0;
        
        try {
            conn = poolConnection.getConnection();
            
            ppstm = conn.prepareStatement("select count(*) as quantity\n"
                    + "from student inner join (\n"
                    + "    select `point`.*\n"
                    + "    from `point`, class\n"
                    + "    where `point`.class_id = class.id\n"
                    + "    and class.id = ?\n"
                    + ") as `subquery`\n"
                    + "on student.id = `subquery`.student_id\n"
                    + (qText != null ? "where student.name like ? or student.email like ?\n" : ""));
            ppstm.setInt(1, classId);
            if (qText != null) {
            	ppstm.setString(2, "%"+qText+"%");
            	ppstm.setString(3, "%"+qText+"%");
            }
            ResultSet rs = ppstm.executeQuery();
            if (rs.next()) {
                quantity = rs.getInt("quantity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ppstm.close();
                conn.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return quantity;
    }

	@Override
	public StudentPointTableDtos getStudentPointTableDtos(int classId, String qText, int page, int itemInPage) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		StudentPointTableDtos studentPointTableDtos = null;

		try {
			conn = poolConnection.getConnection();
			int quantity = this.getQuantityStudentPointTableDtos(classId, qText);
			if (quantity == 0)
				return null;

			List<StudentPointTable> list = new ArrayList<>();
			studentPointTableDtos = new StudentPointTableDtos();
			itemInPage = itemInPage > 0 ? itemInPage : studentPointTableDtos.getItemInPage();

			ppstm = conn.prepareStatement(""
					+ "select student.*,\n"
					+ "`subquery`.id as point_id, `subquery`.cc, `subquery`.btl, `subquery`.th, `subquery`.ktgk, `subquery`.ktck, `subquery`.student_id, `subquery`.class_id\n"
					+ "from student inner join (\n"
					+ "    select `point`.*\n"
					+ "    from `point`, class\n"
					+ "    where `point`.class_id = class.id\n"
					+ "    and class.id = ?\n"
					+ ") as `subquery`\n"
					+ "on student.id = `subquery`.student_id\n"
					+ (qText != null ? "where student.name like ? or student.email like ?\n" : "")
					+ "order by student.name asc, student.id asc\n"
					+ "limit ?, ?");
			ppstm.setInt(1, classId);
			if (qText != null) {
				ppstm.setString(2, "%"+qText+"%");
				ppstm.setString(3, "%"+qText+"%");
				ppstm.setInt(4, (page - 1) * itemInPage);
				ppstm.setInt(5, itemInPage);
			} else {
				ppstm.setInt(2, (page - 1) * itemInPage);
				ppstm.setInt(3, itemInPage);
			}

			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				Student student = new Student();
				student.setId(rs.getInt("id"));
				student.setName(rs.getString("name"));
				student.setEmail(rs.getString("email"));
				student.setPhone(rs.getString("phone"));
				student.setAddress(rs.getString("address"));
				student.setGender(rs.getString("gender"));
				student.setPassword(rs.getString("password"));
				student.setDateOfBirth(new Date(rs.getDate("date_of_birth").getTime()));
				student.setAdministrativeClass(rs.getString("administrative_class"));

				Point point = new Point();
				point.setId(rs.getInt("point_id"));
				point.setClassId(rs.getInt("class_id"));
				point.setStudentId(rs.getInt("student_id"));

				float cc = rs.getFloat("cc");
				if (rs.wasNull()) {
					point.setCc(null);
				} else {
					point.setCc(cc);
				}

				float btl = rs.getFloat("btl");
				if (rs.wasNull()) {
					point.setBtl(null);
				} else {
					point.setBtl(btl);
				}

				float th = rs.getFloat("th");
				if (rs.wasNull()) {
					point.setTh(null);
				} else {
					point.setTh(th);
				}

				float ktgk = rs.getFloat("ktgk");
				if (rs.wasNull()) {
					point.setKtgk(null);
				} else {
					point.setKtgk(ktgk);
				}

				float ktck = rs.getFloat("ktck");
				if (rs.wasNull()) {
					point.setKtck(null);
				} else {
					point.setKtck(ktck);
				}

				StudentPointTable studentPointTable = new StudentPointTable();
				studentPointTable.setStudent(student);
				studentPointTable.setPoint(point);
				list.add(studentPointTable);
			}
			studentPointTableDtos.setItemInPage(itemInPage);
			studentPointTableDtos.setQuantity(quantity);
			studentPointTableDtos.setPage(page);
			studentPointTableDtos.setStudentPointTables(list);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return studentPointTableDtos;
	}

	@Override
	public Subject getSubjectById(int subjectId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Subject subject = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from subject where id = ?");
			ppstm.setInt(1, subjectId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				subject = new Subject();
				subject.setId(rs.getInt("id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return subject;
	}

	private void setFloat(PreparedStatement ppstm, int colIdx, Float value) throws Exception {
		if (value == null) {
			ppstm.setNull(colIdx, java.sql.Types.FLOAT);
			return;
		}
		ppstm.setFloat(colIdx, value);
	}

	@Override
	public void savePoint(int pointId, Float cc, Float btl, Float th, Float ktgk, Float ktck) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(
					"update point\n" + "set cc = ?, btl = ?, th = ?, ktgk = ?, ktck = ?\n" + "where id = ?");
			setFloat(ppstm, 1, cc);
			setFloat(ppstm, 2, btl);
			setFloat(ppstm, 3, th);
			setFloat(ppstm, 4, ktgk);
			setFloat(ppstm, 5, ktck);
			ppstm.setInt(6, pointId);
			ppstm.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	@Override
	public Subject getSubjectByClassId(int classId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Subject subject = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select subject.*\n" + "from subject, class\n"
					+ "where class.id = ? and class.subject_id = subject.id");
			ppstm.setInt(1, classId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				subject = new Subject();
				subject.setId(rs.getInt("id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return subject;
	}

	@Override
	public Subject getSubjectByPointId(int pointId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Subject subject = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select subject.*\n" + "from `point`, class, subject\n"
					+ "where `point`.id = ?\n" + "and `point`.class_id = class.id and class.subject_id = subject.id");
			ppstm.setInt(1, pointId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				subject = new Subject();
				subject.setId(rs.getInt("id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return subject;
	}

	@Override
	public List<StatisticalStudentDto> getStatisticalStudentDtos(int termId, int subjectId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<StatisticalStudentDto> list = new ArrayList<>();
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(
							"select subquery1.accept, subquery2.*, subquery3.class_name, subquery3.subject_id\n"
							+ "from\n"
							+ "(\n"
							+ "    select count(`point`.id) as accept, class.id as class_id\n"
							+ "    from `point`, class\n"
							+ "    where class.term_id = ?\n"
							+ "    and `point`.class_id = class.id\n"
							+ "    and \n"
							+ "    (`point`.cc is null or `point`.cc != 0) and\n"
							+ "    (`point`.btl is null or `point`.btl != 0) and\n"
							+ "    (`point`.th is null or `point`.th != 0) and\n"
							+ "    (`point`.ktgk is null or `point`.ktgk != 0)\n"
							+ "    group by class.id\n"
							+ ") as subquery1\n"
							+ "inner join\n"
							+ "(\n"
							+ "    select count(`point`.id) as reject, class.id as class_id\n"
							+ "    from `point`, class\n"
							+ "    where class.term_id = ?\n"
							+ "    and `point`.class_id = class.id\n"
							+ "    and (\n"
							+ "        `point`.cc = 0 or\n"
							+ "        `point`.btl = 0 or\n"
							+ "        `point`.th = 0 or\n"
							+ "        `point`.ktgk = 0\n"
							+ "    )\n"
							+ "    group by class.id\n"
							+ ") as subquery2\n"
							+ "on subquery1.class_id = subquery2.class_id\n"
							+ "inner join\n"
							+ "(\n"
							+ "    select id as class_id, name as class_name, class.subject_id\n"
							+ "    from class where class.term_id = ?\n"
							+ ") as subquery3\n"
							+ "on subquery2.class_id = subquery3.class_id\n"
							+ "where subquery3.subject_id = ?");
			ppstm.setInt(1, termId);
			ppstm.setInt(2, termId);
			ppstm.setInt(3, termId);
			ppstm.setInt(4, subjectId);

			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				StatisticalStudentDto statisticalStudentDto = new StatisticalStudentDto();
				statisticalStudentDto.setAccept(rs.getInt("accept"));
				statisticalStudentDto.setReject(rs.getInt("reject"));
				statisticalStudentDto.setClassId(rs.getInt("class_id"));
				statisticalStudentDto.setClassName(rs.getString("class_name"));
				list.add(statisticalStudentDto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public Term getTermByPointId(int pointId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Term term = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select term.*\n" + "from `point`, class, term\n"
					+ "where `point`.id = ? and `point`.class_id = class.id\n" + "and class.term_id = term.id");
			ppstm.setInt(1, pointId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return term;
	}

	public Term getTermById(int termId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Term term = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from term where id = ?");
			ppstm.setInt(1, termId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return term;
	}

	public Clazz getClassByPointId(int pointId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Clazz clazz = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select class.*\n" + "from `point`, class\n"
					+ "where `point`.id = ? and `point`.class_id = class.id");
			ppstm.setInt(1, pointId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				clazz = new Clazz();
				clazz.setId(rs.getInt("id"));
				clazz.setName(rs.getString("name"));
				clazz.setRoomName(rs.getString("room_name"));
				clazz.setSubjectId(rs.getInt("subject_id"));
				clazz.setTeacherId(rs.getInt("teacher_id"));
				clazz.setTermId(rs.getInt("term_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return clazz;
	}

	@Override
	public Term getTermByClassId(int classId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Term term = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(
					"select term.*\n" + "from class, term\n" + "where class.id = ? and class.term_id = term.id");
			ppstm.setInt(1, classId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return term;
	}

	@Override
	public Teacher getTeacherByClassId(int classId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Teacher teacher = null;

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select teacher.*\n" + "from class, teacher\n"
					+ "where class.id = ? and class.teacher_id = teacher.id");
			ppstm.setInt(1, classId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				teacher = new Teacher();
				teacher.setId(rs.getInt("id"));
				teacher.setName(rs.getString("name"));
				teacher.setEmail(rs.getString("email"));
				teacher.setPassword(rs.getString("password"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return teacher;
	}

	@Override
	public Student getStudentById(int studentId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Student student = null;

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from student where id = ?");
			ppstm.setInt(1, studentId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				student = new Student();
				student.setId(rs.getInt("id"));
				student.setName(rs.getString("name"));
				student.setEmail(rs.getString("email"));
				student.setPhone(rs.getString("phone"));
				student.setAddress(rs.getString("address"));
				student.setGender(rs.getString("gender"));
				student.setPassword(rs.getString("password"));
				student.setDateOfBirth(new Date(rs.getDate("date_of_birth").getTime()));
				student.setAdministrativeClass(rs.getString("administrative_class"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return student;
	}

	@Override
	public void savePointByClassIdAndStudentId(Connection conn, int classId, int studentId, Float cc, Float btl,
			Float th, Float ktgk, Float ktck) throws Exception {
		PreparedStatement ppstm = conn.prepareStatement("update `point`\n"
				+ "set cc = ?, btl = ?, th = ?, ktgk = ?, ktck = ?\n" + "where class_id = ? and student_id = ?");
		setFloat(ppstm, 1, cc);
		setFloat(ppstm, 2, btl);
		setFloat(ppstm, 3, th);
		setFloat(ppstm, 4, ktgk);
		setFloat(ppstm, 5, ktck);
		ppstm.setInt(6, classId);
		ppstm.setInt(7, studentId);
		ppstm.execute();
	}

	@Override
	public Point getPointByCLassIdAndStudentId(int classId, int studentId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Point point = null;

		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from `point`\n" + "where class_id = ? and student_id = ?");
			ppstm.setInt(1, classId);
			ppstm.setInt(2, studentId);

			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				point = new Point();
				point.setId(rs.getInt("id"));
//                point.setCc(rs.getFloat("cc"));
//                point.setBtl(rs.getFloat("btl"));
//                point.setTh(rs.getFloat("th"));
//                point.setKtgk(rs.getFloat("ktgk"));
//                point.setKtck(rs.getFloat("ktck"));
//                point.setClassId(rs.getInt("class_id"));
//                point.setStudentId(rs.getInt("student_id"));

				float cc = rs.getFloat("cc");
				if (rs.wasNull()) {
					point.setCc(null);
				} else {
					point.setCc(cc);
				}

				float btl = rs.getFloat("btl");
				if (rs.wasNull()) {
					point.setBtl(null);
				} else {
					point.setBtl(btl);
				}

				float th = rs.getFloat("th");
				if (rs.wasNull()) {
					point.setTh(null);
				} else {
					point.setTh(th);
				}

				float ktgk = rs.getFloat("ktgk");
				if (rs.wasNull()) {
					point.setKtgk(null);
				} else {
					point.setKtgk(ktgk);
				}

				float ktck = rs.getFloat("ktck");
				if (rs.wasNull()) {
					point.setKtck(null);
				} else {
					point.setKtck(ktck);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return point;
	}

	@Override
	public void saveTeacherByEmail(Teacher teacher) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("update teacher set name = ?, password = ? where email = ?;");
			ppstm.setString(1, teacher.getName());
			ppstm.setString(2, teacher.getPassword());
			ppstm.setString(3, teacher.getEmail());

			ppstm.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	@Override
	public Student getStudentByEmail(String email) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Student student = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement("select * from student where email = binary ?");
			ppstm.setString(1, email);
			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				student = new Student();
				student.setId(rs.getInt("id"));
				student.setName(rs.getString("name"));
				student.setEmail(rs.getString("email"));
				student.setPhone(rs.getString("phone"));
				student.setAddress(rs.getString("address"));
				student.setGender(rs.getString("gender"));
				student.setPassword(rs.getString("password"));
				student.setDateOfBirth(new Date(rs.getDate("date_of_birth").getTime()));
				student.setAdministrativeClass(rs.getString("administrative_class"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return student;
	}
	
	@Override
	public List<StudentViewScoreDto> getStudentViewScoreDtos(int studentId, int classId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<StudentViewScoreDto> list = new ArrayList<>();
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(""
					+ "select point.id as point_id, point.*, subject.id as subject_id, subject.*\n"
					+ "from point, class, subject\n"
					+ "where point.class_id = class.id\n"
					+ "and class.subject_id = subject.id\n"
					+ "and point.student_id = ?\n"
					+ "and class.term_id = ?");
			ppstm.setInt(1, studentId);
			ppstm.setInt(2, classId);
			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				StudentViewScoreDto studentViewScoreDto = new StudentViewScoreDto();
				
				Subject subject = new Subject();
				subject.setId(rs.getInt("subject_id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
				
				Point point = new Point();
				point.setId(rs.getInt("point_id"));
				point.setClassId(rs.getInt("class_id"));
				point.setStudentId(rs.getInt("student_id"));

				float cc = rs.getFloat("cc");
				if (rs.wasNull()) {
					point.setCc(null);
				} else {
					point.setCc(cc);
				}

				float btl = rs.getFloat("btl");
				if (rs.wasNull()) {
					point.setBtl(null);
				} else {
					point.setBtl(btl);
				}

				float th = rs.getFloat("th");
				if (rs.wasNull()) {
					point.setTh(null);
				} else {
					point.setTh(th);
				}

				float ktgk = rs.getFloat("ktgk");
				if (rs.wasNull()) {
					point.setKtgk(null);
				} else {
					point.setKtgk(ktgk);
				}

				float ktck = rs.getFloat("ktck");
				if (rs.wasNull()) {
					point.setKtck(null);
				} else {
					point.setKtck(ktck);
				}
				
				studentViewScoreDto.setSubject(subject);
				studentViewScoreDto.setPoint(point);
				
				list.add(studentViewScoreDto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}
	
	@Override
	public List<Term> getTermByStudentId(int studentId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<Term> list = new ArrayList<>();
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(""
					+ "select distinct term.*\n"
					+ "from point, class, term\n"
					+ "where point.student_id = ?\n"
					+ "and point.class_id = class.id\n"
					+ "and class.term_id = term.id");
			ppstm.setInt(1, studentId);
			ResultSet rs = ppstm.executeQuery();
			while (rs.next()) {
				Term term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
				
				list.add(term);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}
	
	@Override
	public Student getStudentByPointId(int pointId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		Student student = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(""
					+ "select student.*\n"
					+ "from point, student\n"
					+ "where point.student_id = student.id\n"
					+ "and point.id = ?");
			ppstm.setInt(1, pointId);
			ResultSet rs = ppstm.executeQuery();
			if (rs.next()) {
				student = new Student();
				student.setId(rs.getInt("id"));
				student.setName(rs.getString("name"));
				student.setEmail(rs.getString("email"));
				student.setPhone(rs.getString("phone"));
				student.setAddress(rs.getString("address"));
				student.setGender(rs.getString("gender"));
				student.setPassword(rs.getString("password"));
				student.setDateOfBirth(new Date(rs.getDate("date_of_birth").getTime()));
				student.setAdministrativeClass(rs.getString("administrative_class"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return student;
	}
	
	@Override
	public List<TermAndSubjectsDto> getTermAndSubjectsByStudentId(int studentId) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		List<TermAndSubjectsDto> list = new ArrayList<>();
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(""
					+ "select term.id as term_id, term.*, subject.id as subject_id, subject.*\n"
					+ "from point, class, subject, term\n"
					+ "where point.class_id = class.id\n"
					+ "and class.subject_id = subject.id\n"
					+ "and class.term_id = term.id\n"
					+ "and DATE(now()) >= term.start_date and DATE(now()) <= term.end_date\n"
					+ "and point.student_id = ?\n"
					+ "order by term_id");
			ppstm.setInt(1, studentId);
			ResultSet rs = ppstm.executeQuery();
			List<Subject> subjects = new ArrayList<>();
			Term preTerm = null, term = null;
			int cnt = 0;
			while (rs.next()) {
				Subject subject = new Subject();
				subject.setId(rs.getInt("subject_id"));
				subject.setName(rs.getString("name"));
				subject.setNumberOfCredits(rs.getInt("number_of_credits"));
				subject.setPercentCC(rs.getInt("percent_cc"));
				subject.setPercentBTL(rs.getInt("percent_btl"));
				subject.setPercentTH(rs.getInt("percent_th"));
				subject.setPercentKTGK(rs.getInt("percent_ktgk"));
				subject.setPercentKTCK(rs.getInt("percent_ktck"));
				
				term = new Term();
				term.setId(rs.getInt("id"));
				term.setName(rs.getString("name"));
				term.setStartDate(new Date(rs.getDate("start_date").getTime()));
				term.setEndDate(new Date(rs.getDate("end_date").getTime()));
				
				if (cnt == 0) {
					preTerm = term;
				}
				
				if (term.getId() != preTerm.getId()) {
					TermAndSubjectsDto termAndSubjectsDto = new TermAndSubjectsDto();
					termAndSubjectsDto.setTerm(preTerm);
					termAndSubjectsDto.setSubjects(subjects);
					list.add(termAndSubjectsDto);
					
					subjects = new ArrayList<>();
				}
				subjects.add(subject);
				preTerm = term;
				cnt++;
			}
			if (cnt > 0) {				
				TermAndSubjectsDto termAndSubjectsDto = new TermAndSubjectsDto();
				termAndSubjectsDto.setTerm(preTerm);
				termAndSubjectsDto.setSubjects(subjects);
				list.add(termAndSubjectsDto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}
	
	@Override
	public void saveStudentByEmail(Student student) {
		Connection conn = null;
		PreparedStatement ppstm = null;
		try {
			conn = poolConnection.getConnection();
			ppstm = conn.prepareStatement(""
					+ "update student set name = ?, phone = ?, address = ?, gender = ?,\n"
					+ "password = ?, date_of_birth = ?, administrative_class = ?\n"
					+ "where email = binary ?");
			ppstm.setString(1, student.getName());
			ppstm.setString(2, student.getPhone());
			ppstm.setString(3, student.getAddress());
			ppstm.setString(4, student.getGender());
			ppstm.setString(5, student.getPassword());
			ppstm.setDate(6, new java.sql.Date(student.getDateOfBirth().getTime()));
			ppstm.setString(7, student.getAdministrativeClass());
			ppstm.setString(8, student.getEmail());
			
			ppstm.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ppstm.close();
				conn.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
