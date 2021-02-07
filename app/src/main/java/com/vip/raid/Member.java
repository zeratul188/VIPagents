package com.vip.raid;

public class Member {
    private String name, grade, date, startdate;
    private int date_length;
    private boolean isUnconnect;

    public Member(String name, String grade, String date, String startdate, int date_length, boolean isUnconnect) {
        this.name = name;
        this.grade = grade;
        this.date = date;
        this.startdate = startdate;
        this.date_length = date_length;
        this.isUnconnect = isUnconnect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public int getDate_length() {
        return date_length;
    }

    public void setDate_length(int date_length) {
        this.date_length = date_length;
    }

    public boolean isUnconnect() {
        return isUnconnect;
    }

    public void setUnconnect(boolean unconnect) {
        isUnconnect = unconnect;
    }
}
