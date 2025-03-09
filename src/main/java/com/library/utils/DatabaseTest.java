package com.library.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            // ทดสอบการเชื่อมต่อ
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("เชื่อมต่อกับฐานข้อมูลสำเร็จ!");

            // ทดสอบการดึงข้อมูล
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM books");

            System.out.println("รายการหนังสือ:");
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");

                System.out.println(id + " - " + title + " by " + author);
            }

            // ปิดการเชื่อมต่อ
            DatabaseConnection.closeConnection();

        } catch (SQLException e) {
            System.err.println("เกิดข้อผิดพลาด: " + e.getMessage());
            e.printStackTrace();
        }
    }
}