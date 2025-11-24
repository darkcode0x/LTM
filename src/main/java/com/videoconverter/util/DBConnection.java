package com.videoconverter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/video_converter";
    private static final String USER = "videouser";
    private static final String PASSWORD = "videopass";
    
    // Load MySQL driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            USER,
            PASSWORD
        );
    }
}
