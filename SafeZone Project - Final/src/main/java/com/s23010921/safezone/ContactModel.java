package com.s23010921.safezone;

public class ContactModel {

    private int id;
    private String fname, lname, number, priority;

    public ContactModel(int id,String fname, String lname, String number, String priority) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.number = number;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fname + " " + lname;
    }

    public String getNumber() {
        return number;
    }

    public String getPriority() {
        return priority;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }
}
