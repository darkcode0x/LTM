package com.videoconverter.dao;

import com.videoconverter.model.User;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object for User operations
 * Handles all database operations related to users table
 */
public class UserDAO {

    /**
     * Find user by username or email
     * 
     * @param usernameOrEmail Username or email to search for
     * @return User object if found, null otherwise
     */
    public User findByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username or email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find user by ID
     * 
     * @param userId User ID
     * @return User object if found, null otherwise
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Insert a new user into database
     * 
     * @param user User object to insert
     * @return Generated user ID, or -1 if failed
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, email, password, full_name, phone, avatar, " +
                     "is_active, daily_quota, total_conversions) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getAvatar());
            stmt.setBoolean(7, user.isActive());
            stmt.setInt(8, user.getDailyQuota());
            stmt.setInt(9, user.getTotalConversions());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        user.setUserId(userId);
                        System.out.println("User inserted successfully with ID: " + userId);
                        return userId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Check if username already exists
     * 
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Check if email already exists
     * 
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Update user's last login timestamp
     * 
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Last login updated for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Update user profile information
     * 
     * @param user User object with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, avatar = ?, " +
                     "email = ?, is_active = ?, daily_quota = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getAvatar());
            stmt.setString(4, user.getEmail());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getDailyQuota());
            stmt.setInt(7, user.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("User updated successfully: " + user.getUserId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Update password for a user
     * 
     * @param userId User ID
     * @param hashedPassword New hashed password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Password updated for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Increment total conversions count
     * 
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public boolean incrementTotalConversions(int userId) {
        String sql = "UPDATE users SET total_conversions = total_conversions + 1 WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing total conversions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update user active status (Admin only)
     * 
     * @param userId User ID
     * @param isActive New active status
     * @return true if successful, false otherwise
     */
    public boolean updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("User status updated: ID=" + userId + ", Active=" + isActive);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update user daily quota (Admin only)
     * 
     * @param userId User ID
     * @param quota New daily quota
     * @return true if successful, false otherwise
     */
    public boolean updateUserQuota(int userId, int quota) {
        String sql = "UPDATE users SET daily_quota = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quota);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("User quota updated: ID=" + userId + ", Quota=" + quota);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user quota: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all users (for admin purposes)
     * 
     * @return List of all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Alias for findAll() - Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return findAll();
    }

    /**
     * Delete a user (cascade will delete related records)
     * 
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("User deleted successfully: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Extract User object from ResultSet
     * 
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if error reading ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setAvatar(rs.getString("avatar"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setActive(rs.getBoolean("is_active"));
        user.setDailyQuota(rs.getInt("daily_quota"));
        user.setTotalConversions(rs.getInt("total_conversions"));
        return user;
    }
}
