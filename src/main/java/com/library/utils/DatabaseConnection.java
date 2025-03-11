package com.library.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Austin1234509";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        int retries = 3;
        while (retries > 0) {
            try {
                if (connection == null || connection.isClosed()) {
                    Properties props = new Properties();
                    props.setProperty("user", DB_USER);
                    props.setProperty("password", DB_PASSWORD);
                    props.setProperty("useSSL", "false");
                    props.setProperty("allowPublicKeyRetrieval", "true");
                    props.setProperty("serverTimezone", "UTC");

                    connection = DriverManager.getConnection(DB_URL, props);
                    System.out.println("Database connection established successfully");
                }
                return connection;
            } catch (SQLException e) {
                System.err.println(" Database connection error: " + e.getMessage());
                retries--;
                if (retries == 0) {
                    throw e; // ถ้าลองครบ 3 ครั้งแล้วยังไม่ได้ ก็โยน Exception ออกไป
                }
                System.out.println("Retrying to connect...");
                try {
                    Thread.sleep(2000); // รอ 2 วินาทีก่อนลองใหม่
                } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // ปิด connection อัตโนมัติเมื่อโปรแกรมปิด
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}