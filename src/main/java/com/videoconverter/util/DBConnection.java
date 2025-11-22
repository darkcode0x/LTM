package com.videoconverter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    

    private static final String URL = "jdbc:mysql://localhost:3306/video_converter";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // MySQL driver class name
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings (optional - for future enhancement)
    private static final String CONNECTION_PROPERTIES = 
            "?useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true" +
            "&useUnicode=true" +
            "&characterEncoding=UTF-8";
    
    // Static block to load MySQL driver
    static {
        try {
            Class.forName(DRIVER);
            System.out.println("MySQL JDBC Driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }
    

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(
                URL + CONNECTION_PROPERTIES, 
                USER, 
                PASSWORD
            );
            System.out.println("Database connection established successfully!");
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection!");
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed successfully!");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getDatabaseURL() {
        return URL;
    }

    public static String getDatabaseUser() {
        return USER;
    }
    
    // Main method for testing connection
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        System.out.println("========================================");
        System.out.println("Database URL: " + URL);
        System.out.println("Database User: " + USER);
        System.out.println("========================================");
        
        if (testConnection()) {
            System.out.println("✓ Connection test PASSED!");
        } else {
            System.out.println("✗ Connection test FAILED!");
            System.out.println("\nPlease check:");
            System.out.println("1. MySQL server is running");
            System.out.println("2. Database 'video_converter' exists");
            System.out.println("3. Username and password are correct");
            System.out.println("4. MySQL Connector/J dependency is in classpath");
        }
    }
}
