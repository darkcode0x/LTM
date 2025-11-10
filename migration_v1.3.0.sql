-- Migration Script: Add Admin Features & Activity Logs
-- Version: 1.3.0
-- Date: November 7, 2025
-- Description: Adds role column to users table and enables activity logging

-- Step 1: Add role column to users table
ALTER TABLE users ADD COLUMN role ENUM('USER', 'ADMIN') DEFAULT 'USER' AFTER avatar;

-- Step 2: Update existing admin user
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';

-- Step 3: Verify changes
SELECT user_id, username, email, role, is_active FROM users;

-- Step 4: Check activity_logs table exists (should already exist from database.sql)
SHOW TABLES LIKE 'activity_logs';

-- Step 5: Verify activity_logs structure
DESC activity_logs;

-- Optional: Add some test activity logs
-- INSERT INTO activity_logs (user_id, action, description, ip_address) VALUES
-- (1, 'LOGIN', 'User logged in successfully', '127.0.0.1'),
-- (1, 'UPLOAD', 'Uploaded video: test.mp4', '127.0.0.1'),
-- (2, 'LOGIN', 'User logged in successfully', '192.168.1.100');

-- Verify data
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as admin_count FROM users WHERE role = 'ADMIN';
SELECT COUNT(*) as user_count FROM users WHERE role = 'USER';
SELECT COUNT(*) as total_logs FROM activity_logs;

-- Show recent activity
SELECT l.log_id, u.username, l.action, l.description, l.created_at 
FROM activity_logs l 
JOIN users u ON l.user_id = u.user_id 
ORDER BY l.created_at DESC 
LIMIT 10;
