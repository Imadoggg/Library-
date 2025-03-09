package com.library.services;

import com.library.models.User;
import com.library.models.UserRole;

public class AuthenticationService {

    private static User currentUser = null;

    public static boolean login(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            currentUser = new User(username, password, UserRole.ADMIN, "ผู้ดูแลระบบ", "admin@library.com");
            return true;
        } else if ("user".equals(username) && "user".equals(password)) {
            currentUser = new User(username, password, UserRole.USER, "ผู้ใช้งานทั่วไป", "user@library.com");
            return true;
        }

        return false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}