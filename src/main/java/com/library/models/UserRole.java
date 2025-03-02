package com.library.models;

public enum UserRole {
    ADMIN("ผู้ดูแลระบบ"),
    USER("ผู้ใช้งาน");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}