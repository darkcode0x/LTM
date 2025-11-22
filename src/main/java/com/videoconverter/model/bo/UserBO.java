package com.videoconverter.model.bo;

import com.videoconverter.model.bean.User;
import com.videoconverter.model.dao.UserDAO;
import com.videoconverter.util.PasswordUtil;

/**
 * UserBO - Business logic for user operations
 */
public class UserBO {
    private final UserDAO userDAO;

    public UserBO() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user with username and password
     */
    public User authenticate(String username, String password) {
        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            return null;
        }

        if (PasswordUtil.checkPassword(password, user.getPassword())) {
            return user;
        } else {
            return null;
        }
    }

    /**
     * Register new user
     */
    public boolean register(String username, String password, String email) {
        // Check if username exists
        if (userDAO.getUserByUsername(username) != null) {
            return false;
        }
        // Check if email exists
        if (userDAO.getUserByEmail(email) != null) {
            return false;
        }

        // Hash password and create user
        String hashedPassword = PasswordUtil.hashPassword(password);
        User user = new User(username, hashedPassword, email);

        return userDAO.createUser(user);
    }

    /**
     * Get total number of users (for admin)
     */
    public int getTotalUsers() {
        return userDAO.getTotalUsers();
    }

    /**
     * Validate username format
     */
    public boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}


