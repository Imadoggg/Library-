package com.library.models;

public enum UserRole {
    ADMIN("ผู้ดูแลระบบ"),
    USER("ผู้ใช้งาน");


    private final String thaiDescription;



    UserRole(String thaiDescription) {
        this.thaiDescription = thaiDescription;
    }

    public String getDisplayName() {
        return thaiDescription;
    }


    public String getThaiDescription() {
        return thaiDescription;
    }

    @Override
    public String toString() {
        return thaiDescription;
    }
}