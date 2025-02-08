package com.library.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BorrowRecord {
    private final StringProperty id = new SimpleStringProperty();
    private final ObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObjectProperty<Member> member = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> borrowDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> returnDate = new SimpleObjectProperty<>();
    private final BooleanProperty returned = new SimpleBooleanProperty(false);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BorrowRecord(Book book, Member member) {
        setId("BR" + System.currentTimeMillis());
        setBook(book);
        setMember(member);
        setBorrowDate(LocalDateTime.now());
    }

    // ID
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    // Book
    public Book getBook() { return book.get(); }
    public void setBook(Book value) { book.set(value); }
    public ObjectProperty<Book> bookProperty() { return book; }

    // Member
    public Member getMember() { return member.get(); }
    public void setMember(Member value) { member.set(value); }
    public ObjectProperty<Member> memberProperty() { return member; }

    // Borrow Date
    public LocalDateTime getBorrowDate() { return borrowDate.get(); }
    public void setBorrowDate(LocalDateTime value) { borrowDate.set(value); }
    public ObjectProperty<LocalDateTime> borrowDateProperty() { return borrowDate; }
    public String getFormattedBorrowDate() {
        return borrowDate.get() != null ? borrowDate.get().format(formatter) : "";
    }

    // Return Date
    public LocalDateTime getReturnDate() { return returnDate.get(); }
    public void setReturnDate(LocalDateTime value) { returnDate.set(value); }
    public ObjectProperty<LocalDateTime> returnDateProperty() { return returnDate; }
    public String getFormattedReturnDate() {
        return returnDate.get() != null ? returnDate.get().format(formatter) : "";
    }

    // Returned Status
    public boolean isReturned() { return returned.get(); }
    public void setReturned(boolean value) { returned.set(value); }
    public BooleanProperty returnedProperty() { return returned; }

    public void returnBook() {
        setReturnDate(LocalDateTime.now());
        setReturned(true);
        getBook().setAvailable(true);
    }
}