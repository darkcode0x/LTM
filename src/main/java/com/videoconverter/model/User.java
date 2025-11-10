package com.videoconverter.model;

import java.sql.Timestamp;

/**
 * User Model - Represents users table in database
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String avatar;
    private String role; // USER or ADMIN
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isActive;
    private int dailyQuota;
    private int totalConversions;

    // Default constructor
    public User() {
        this.avatar = "images/default-avatar.png";
        this.role = "USER";
        this.isActive = true;
        this.dailyQuota = 5;
        this.totalConversions = 0;
    }

    // Constructor without ID (for new user registration)
    public User(String username, String email, String password, String fullName) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // Full constructor
    public User(int userId, String username, String email, String password, String fullName, 
                String phone, String avatar, String role, Timestamp createdAt, Timestamp lastLogin, 
                boolean isActive, int dailyQuota, int totalConversions) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.dailyQuota = dailyQuota;
        this.totalConversions = totalConversions;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getDailyQuota() {
        return dailyQuota;
    }

    public void setDailyQuota(int dailyQuota) {
        this.dailyQuota = dailyQuota;
    }

    public int getTotalConversions() {
        return totalConversions;
    }

    public void setTotalConversions(int totalConversions) {
        this.totalConversions = totalConversions;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", dailyQuota=" + dailyQuota +
                ", totalConversions=" + totalConversions +
                '}';
    }
}
