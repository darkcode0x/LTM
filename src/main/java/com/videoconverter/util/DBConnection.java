package com.videoconverter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Read environment variables (Docker) with local fallbacks
    private static final String DB_HOST = getenv("DB_HOST", "localhost");
    private static final String DB_PORT = getenv("DB_PORT", "3306");
    private static final String DB_NAME = getenv("DB_NAME", "video_converter");
    private static final String DB_USER = getenv("DB_USER", "root");
    private static final String DB_PASSWORD = getenv("DB_PASSWORD", "");

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC";

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBConnection] Loaded MySQL driver. Using URL: " + JDBC_URL);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
}
