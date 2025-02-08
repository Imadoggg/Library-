// src/com/library/models/BorrowRecord.java
package com.library.controllers.models;

import java.time.LocalDateTime;

public class BorrowRecord {
    private String id;
    private Book book;
    private Member member;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private boolean isReturned;

    public BorrowRecord(Book book, Member member) {
        this.id = generateId();
        this.book = book;
        this.member = member;
        this.borrowDate = LocalDateTime.now();
        this.isReturned = false;
    }

    private String generateId() {
        return "BR" + System.currentTimeMillis();
    }

    public void returnBook() {
        this.returnDate = LocalDateTime.now();
        this.isReturned = true;
    }

    // Getters
    public String getId() { return id; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public boolean isReturned() { return isReturned; }
}