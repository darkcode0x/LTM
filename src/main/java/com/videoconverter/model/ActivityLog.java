package com.videoconverter.model;

import java.sql.Timestamp;

/**
 * ActivityLog Model - Represents activity_logs table in database
 */
public class ActivityLog {
    private int logId;
    private int userId;
    private String action;         // LOGIN, LOGOUT, UPLOAD, DOWNLOAD, DELETE, UPDATE_PROFILE, etc.
    private String description;
    private String ipAddress;
    private Timestamp createdAt;
    
    // For display purposes
    private String username;       // Joined from users table
    private String userEmail;      // Joined from users table

    // Default constructor
    public ActivityLog() {
    }

    // Constructor without ID (for new log)
    public ActivityLog(int userId, String action, String description, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    // Full constructor
    public ActivityLog(int logId, int userId, String action, String description, 
                      String ipAddress, Timestamp createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", description='" + description + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
