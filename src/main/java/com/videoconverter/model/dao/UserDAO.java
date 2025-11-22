package com.videoconverter.model.dao;

import com.videoconverter.model.bean.User;
import com.videoconverter.util.DBConnection;

import java.sql.*;

/**
 * UserDAO - Database access for users table
 */
public class UserDAO {

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            System.out.println("[UserDAO] Searching for username: " + username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = extractUser(rs);
                System.out.println("[UserDAO] User found: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
                return user;
            } else {
                System.out.println("[UserDAO] User not found: " + username);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            System.out.println("[UserDAO] Searching for email: " + email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = extractUser(rs);
                System.out.println("[UserDAO] User found by email: " + user.getUsername());
                return user;
            } else {
                System.out.println("[UserDAO] User not found by email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'USER'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private User extractUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getTimestamp("created_at")
        );
    }
}


