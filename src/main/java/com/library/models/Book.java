package com.library.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;

public class Book {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty author = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final BooleanProperty available = new SimpleBooleanProperty(true);

    public Book(String id, String title, String author, String category) {
        setId(id);
        setTitle(title);
        setAuthor(author);
        setCategory(category);
    }

    // ID
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    // Title
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    // Author
    public String getAuthor() { return author.get(); }
    public void setAuthor(String value) { author.set(value); }
    public StringProperty authorProperty() { return author; }

    // Category
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    // Available
    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean value) { available.set(value); }
    public BooleanProperty availableProperty() { return available; }
}