package com.library.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

public class User {
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<UserRole> role = new SimpleObjectProperty<>();
    private final StringProperty email = new SimpleStringProperty();


    public User(String username, String password, UserRole role, String name, String email) {
        setUsername(username);
        setPassword(password);
        setRole(role);
        setName(name);
        setEmail(email);
    }

    // Username
    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    // Password
    public String getPassword() { return password.get(); }
    public void setPassword(String value) { password.set(value); }
    public StringProperty passwordProperty() { return password; }

    // Name
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // Role
    public UserRole getRole() { return role.get(); }
    public void setRole(UserRole value) { role.set(value); }
    public ObjectProperty<UserRole> roleProperty() { return role; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }


    // toString for debugging
    @Override
    public String toString() {
        return "User{" +
                "username=" + username.get() +
                ", name=" + name.get() +
                ", role=" + role.get() +
                '}';
    }
}