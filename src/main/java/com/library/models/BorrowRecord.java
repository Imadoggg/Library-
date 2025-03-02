package com.library.models;

import javafx.beans.property.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BorrowRecord {
    private final StringProperty id = new SimpleStringProperty();
    private final ObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObjectProperty<Member> member = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> borrowDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> returnDate = new SimpleObjectProperty<>();
    private final BooleanProperty returned = new SimpleBooleanProperty(false);

    private static final long DEFAULT_LOAN_DAYS = 14;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BorrowRecord(Book book, Member member) {
        setId("BR" + System.currentTimeMillis());
        setBook(book);
        setMember(member);
        setBorrowDate(LocalDateTime.now());
    }

    public LocalDateTime calculateDueDate() {
        return borrowDate.get().plusDays(DEFAULT_LOAN_DAYS);
    }

    public boolean isOverdue() {
        // ถ้าคืนแล้ว ไม่ถือว่าค้างส่ง
        if (isReturned()) return false;

        // ตรวจสอบวันครบกำหนด
        LocalDateTime dueDate = calculateDueDate();
        return LocalDateTime.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;

        LocalDateTime dueDate = calculateDueDate();
        return Duration.between(dueDate, LocalDateTime.now()).toDays();
    }

    // เพิ่มเมธอดคำนวณค่าปรับ (ถ้ามี)
    public double calculateLateFee(double feePerDay) {
        if (!isOverdue()) return 0.0;
        return getDaysOverdue() * feePerDay;
    }


    // ID
    public String getId() {
        return id.get();
    }

    public void setId(String value) {
        id.set(value);
    }

    public StringProperty idProperty() {
        return id;
    }

    // Book
    public Book getBook() {
        return book.get();
    }

    public void setBook(Book value) {
        book.set(value);
    }

    public ObjectProperty<Book> bookProperty() {
        return book;
    }

    // Member
    public Member getMember() {
        return member.get();
    }

    public void setMember(Member value) {
        member.set(value);
    }

    public ObjectProperty<Member> memberProperty() {
        return member;
    }

    // Borrow Date
    public LocalDateTime getBorrowDate() {
        return borrowDate.get();
    }

    public void setBorrowDate(LocalDateTime value) {
        borrowDate.set(value);
    }

    public ObjectProperty<LocalDateTime> borrowDateProperty() {
        return borrowDate;
    }



    // Return Date
    public LocalDateTime getReturnDate() {
        return returnDate.get();
    }

    public void setReturnDate(LocalDateTime value) {
        returnDate.set(value);
    }

    public ObjectProperty<LocalDateTime> returnDateProperty() {
        return returnDate;
    }



    // Returned Status
    public boolean isReturned() {
        return returned.get();
    }

    public void setReturned(boolean value) {
        returned.set(value);
    }

    public BooleanProperty returnedProperty() {
        return returned;
    }

    public void returnBook() {
        setReturnDate(LocalDateTime.now());
        setReturned(true);
        getBook().setAvailable(true);
    }

    public String getBorrowStatus() {
        if (isReturned()) return "คืนแล้ว";
        if (isOverdue()) return "เกินกำหนด";
        return "รอคืน";
    }

    public String getFormattedBorrowDate() {
        return borrowDate.get() != null ? borrowDate.get().format(formatter) : "";
    }

    public String getFormattedReturnDate() {
        return returnDate.get() != null ? returnDate.get().format(formatter) : "";
    }

}