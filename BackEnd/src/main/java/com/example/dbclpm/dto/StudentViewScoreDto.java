package com.example.dbclpm.dto;

import com.example.dbclpm.model.Point;
import com.example.dbclpm.model.Subject;

public class StudentViewScoreDto {
	private Subject subject;
	private Point point;
	private PointExtent pointExtent;
	
	public StudentViewScoreDto() {}

	public StudentViewScoreDto(Subject subject, Point point, PointExtent pointExtent) {
		super();
		this.subject = subject;
		this.point = point;
		this.pointExtent = pointExtent;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public PointExtent getPointExtent() {
		return pointExtent;
	}

	public void setPointExtent(PointExtent pointExtent) {
		this.pointExtent = pointExtent;
	}

	@Override
	public String toString() {
		return "StudentViewScoreDto [subject=" + subject + ", point=" + point + ", pointExtent=" + pointExtent + "]";
	}
}
