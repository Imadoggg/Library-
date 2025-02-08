package com.library.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Member {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty contact = new SimpleStringProperty();
    private final ObservableList<BorrowRecord> borrowHistory = FXCollections.observableArrayList();

    public Member(String id, String name, String contact) {
        setId(id);
        setName(name);
        setContact(contact);
    }

    // ID
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    // Name
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // Contact
    public String getContact() { return contact.get(); }
    public void setContact(String value) { contact.set(value); }
    public StringProperty contactProperty() { return contact; }

    // Borrow History
    public ObservableList<BorrowRecord> getBorrowHistory() { return borrowHistory; }
    public void addBorrowRecord(BorrowRecord record) { borrowHistory.add(record); }
}