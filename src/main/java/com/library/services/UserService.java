package com.library.services;

import com.library.models.User;
import com.library.models.UserRole;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserService {
    // Simulated user database (in a real application, this would be a database)
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, String> userPasswords = new HashMap<>();

    // Email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    // Static initializer to create some default users
    static {
        // Admin user
        User admin = new User("admin", "admin", UserRole.ADMIN, "ผู้ดูแลระบบ", "admin@library.com");
        users.put("admin", admin);
        userPasswords.put("admin", "admin");

        // Regular user
        User user = new User("user", "user", UserRole.USER, "ผู้ใช้งานทั่วไป", "user@library.com");
        users.put("user", user);
        userPasswords.put("user", "user");
    }

    // Update user profile with validation
    public boolean updateProfile(String username, String fullName, String email) {
        User user = users.get(username);
        if (user != null) {
            user.setName(fullName);
            return true;
        }
        return false;
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Change user password with validation
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("ชื่อผู้ใช้ไม่สามารถเป็นค่าว่างได้");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("รหัสผ่านเก่าไม่สามารถเป็นค่าว่างได้");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("รหัสผ่านใหม่ไม่สามารถเป็นค่าว่างได้");
        }

        // Check if the username exists and old password is correct
        if (userPasswords.containsKey(username) &&
                userPasswords.get(username).equals(oldPassword)) {

            // Update password
            userPasswords.put(username, newPassword);
            return true;
        }
        return false;
    }

    // Get user by username
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    // Validate user credentials
    public boolean validateCredentials(String username, String password) {
        return userPasswords.containsKey(username) &&
                userPasswords.get(username).equals(password);
    }

    // Add new user (for registration) with validation
    public boolean addUser(String username, String password, String fullName, String email, String role) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("ชื่อผู้ใช้ไม่สามารถเป็นค่าว่างได้");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("รหัสผ่านไม่สามารถเป็นค่าว่างได้");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("ชื่อ-นามสกุลไม่สามารถเป็นค่าว่างได้");
        }

        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("รูปแบบอีเมลไม่ถูกต้อง");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("บทบาทไม่สามารถเป็นค่าว่างได้");
        }

        // Check if username already exists
        if (users.containsKey(username)) {
            return false;
        }

        // Create new user
        User newUser = new User(username, password, UserRole.USER, fullName, email);
        users.put(username, newUser);
        userPasswords.put(username, password);
        return true;
    }

    // Delete user
    public boolean deleteUser(String username) {
        if (users.containsKey(username)) {
            users.remove(username);
            userPasswords.remove(username);
            return true;
        }
        return false;
    }
}