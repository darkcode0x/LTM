package com.videoconverter.dao;

import com.videoconverter.model.ActivityLog;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ActivityLogDAO - Data Access Object for activity_logs table
 */
public class ActivityLogDAO {

    /**
     * Create new activity log
     * 
     * @param log ActivityLog object
     * @return true if successful, false otherwise
     */
    public boolean createLog(ActivityLog log) {
        String sql = "INSERT INTO activity_logs (user_id, action, description, ip_address) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getDescription());
            stmt.setString(4, log.getIpAddress());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    log.setLogId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating activity log: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all logs for a specific user
     * 
     * @param userId User ID
     * @return List of ActivityLog objects
     */
    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = extractLogFromResultSet(rs);
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting logs by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }

    /**
     * Get all logs with user information (for admin)
     * 
     * @param limit Maximum number of logs to retrieve
     * @return List of ActivityLog objects with user info
     */
    public List<ActivityLog> getAllLogsWithUserInfo(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username, u.email " +
                    "FROM activity_logs l " +
                    "JOIN users u ON l.user_id = u.user_id " +
                    "ORDER BY l.created_at DESC " +
                    "LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = extractLogFromResultSet(rs);
                log.setUsername(rs.getString("username"));
                log.setUserEmail(rs.getString("email"));
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all logs with user info: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }

    /**
     * Get logs by action type
     * 
     * @param action Action type (LOGIN, UPLOAD, etc.)
     * @param limit Maximum number of logs
     * @return List of ActivityLog objects
     */
    public List<ActivityLog> getLogsByAction(String action, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username, u.email " +
                    "FROM activity_logs l " +
                    "JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.action = ? " +
                    "ORDER BY l.created_at DESC " +
                    "LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, action);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = extractLogFromResultSet(rs);
                log.setUsername(rs.getString("username"));
                log.setUserEmail(rs.getString("email"));
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting logs by action: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }

    /**
     * Get recent logs (last N hours)
     * 
     * @param hours Number of hours to look back
     * @param limit Maximum number of logs
     * @return List of ActivityLog objects
     */
    public List<ActivityLog> getRecentLogs(int hours, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username, u.email " +
                    "FROM activity_logs l " +
                    "JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.created_at >= NOW() - INTERVAL ? HOUR " +
                    "ORDER BY l.created_at DESC " +
                    "LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hours);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = extractLogFromResultSet(rs);
                log.setUsername(rs.getString("username"));
                log.setUserEmail(rs.getString("email"));
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting recent logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }

    /**
     * Count logs by user
     * 
     * @param userId User ID
     * @return Number of logs
     */
    public int countLogsByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM activity_logs WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Delete old logs (cleanup)
     * 
     * @param days Delete logs older than this many days
     * @return Number of deleted logs
     */
    public int deleteOldLogs(int days) {
        String sql = "DELETE FROM activity_logs " +
                    "WHERE created_at < NOW() - INTERVAL ? DAY";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting old logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Extract ActivityLog from ResultSet
     */
    private ActivityLog extractLogFromResultSet(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setDescription(rs.getString("description"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
