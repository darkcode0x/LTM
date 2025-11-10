-- Database
CREATE DATABASE video_converter;
USE video_converter;

-- 1. Bảng Users
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255) DEFAULT 'default-avatar.png',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    daily_quota INT DEFAULT 5,
    total_conversions INT DEFAULT 0
);

-- 2. Bảng Videos (video gốc được upload)
CREATE TABLE videos (
    video_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    duration INT,              -- giây
    resolution VARCHAR(20),    -- 1920x1080
    format VARCHAR(20),        -- mp4, avi...
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Bảng Conversion Jobs
CREATE TABLE conversion_jobs (
    job_id INT PRIMARY KEY AUTO_INCREMENT,
    video_id INT NOT NULL,
    user_id INT NOT NULL,
    
    -- Trạng thái
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    progress INT DEFAULT 0,    -- 0-100%
    priority INT DEFAULT 0,     -- cao hơn = ưu tiên hơn
    
    -- Cấu hình conversion
    output_format VARCHAR(20) NOT NULL,        -- mp4, avi, mkv...
    output_resolution VARCHAR(20),             -- 1280x720
    quality VARCHAR(20),                       -- high, medium, low
    video_bitrate VARCHAR(20),                 -- 2000k
    audio_bitrate VARCHAR(20),                 -- 192k
    codec VARCHAR(20) DEFAULT 'libx264',       -- libx264, libx265
    frame_rate INT DEFAULT 30,
    
    -- Cắt video (optional)
    start_time INT NULL,       -- giây
    end_time INT NULL,         -- giây
    
    -- Kết quả
    output_filename VARCHAR(255),
    output_path VARCHAR(500),
    output_size BIGINT,
    
    -- Thời gian
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    estimated_time INT,        -- giây ước tính
    
    -- Error handling
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    
    FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Bảng Activity Log (optional - theo dõi hoạt động)
CREATE TABLE activity_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,  -- LOGIN, UPLOAD, DOWNLOAD, DELETE
    description TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Indexes để tăng tốc query
CREATE INDEX idx_user_jobs ON conversion_jobs(user_id, status);
CREATE INDEX idx_job_status ON conversion_jobs(status, created_at);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- Sample data
INSERT INTO users (username, email, password, full_name, role) VALUES
('admin', 'admin@example.com', '$2a$10$txf3MKgnUem8haOOzyniT.tcEJa4PbX9jxtEYtosGXvH4zY5jbhI6', 'Administrator', 'ADMIN'),
('demo', 'demo@example.com', '$2a$10$Yk72pwsi22JjALmaPDB9FOxLk.V5beCNhQiSdCQPTks77i5EVyfEW', 'Demo User', 'USER');