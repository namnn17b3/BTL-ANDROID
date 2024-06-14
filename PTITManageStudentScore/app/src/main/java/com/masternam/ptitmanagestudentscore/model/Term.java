package com.masternam.ptitmanagestudentscore.model;

import java.util.Date;

public class Term {
    private int id;
    private String name;
    private String startDate;
    private String endDate;

    public Term() {}

    public Term(int id, String name, String startDate, String endDate) {
        super();
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Term [id=" + id + ", name=" + name + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }
}
