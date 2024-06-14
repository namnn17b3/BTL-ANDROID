package com.example.dbclpm.dto;

import java.util.List;

import com.example.dbclpm.model.Subject;
import com.example.dbclpm.model.Term;

public class TermAndSubjectsDto {
	
	private Term term;
	private List<Subject> subjects;
	
	public TermAndSubjectsDto() {}

	public TermAndSubjectsDto(Term term, List<Subject> subjects) {
		super();
		this.term = term;
		this.subjects = subjects;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public List<Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}

	@Override
	public String toString() {
		return "TermAndSubjectsDto [term=" + term + ", subjects=" + subjects + "]";
	}
}
