package com.library.controllers.models;

import java.util.ArrayList;
import java.util.List;

public class Member {
    private String id;
    private String name;
    private String contact;
    private List<BorrowRecord> borrowHistory;

    public Member(String id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.borrowHistory = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public List<BorrowRecord> getBorrowHistory() { return borrowHistory; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void addBorrowRecord(BorrowRecord record) {
        borrowHistory.add(record);
    }
}