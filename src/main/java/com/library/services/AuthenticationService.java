package com.library.services;

import com.library.models.User;
import com.library.models.UserRole;

public class AuthenticationService {
    // Singleton instance of the currently logged-in user
    private static User currentUser = null;

    // Authenticate user
    public static boolean login(String username, String password) {
        // TODO: Implement actual authentication logic
        // This is a placeholder implementation
        if ("admin".equals(username) && "admin".equals(password)) {
            currentUser = new User(username, password, UserRole.ADMIN, "ผู้ดูแลระบบ", "admin@library.com");
            return true;
        } else if ("user".equals(username) && "user".equals(password)) {
            currentUser = new User(username, password, UserRole.USER, "ผู้ใช้งานทั่วไป", "user@library.com");
            return true;
        }

        // Return false if no matching credentials are found
        return false;
    }

    // Get current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Logout
    public static void logout() {
        currentUser = null;
    }

    // Check if user is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}