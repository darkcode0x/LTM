-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 07, 2025 lúc 04:53 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `video_converter`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `activity_logs`
--

CREATE TABLE `activity_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `activity_logs`
--

INSERT INTO `activity_logs` (`log_id`, `user_id`, `action`, `description`, `ip_address`, `created_at`) VALUES
(1, 1, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 14:13:37'),
(2, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 14:15:31'),
(3, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 14:16:34'),
(4, 1, 'ADMIN_ACTION', 'Admin admin deactivated user John (ID: 4)', '0:0:0:0:0:0:0:1', '2025-11-07 14:18:03'),
(5, 3, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 14:19:51'),
(6, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 14:38:54'),
(7, 3, 'LOGOUT', 'User logged out', '0:0:0:0:0:0:0:1', '2025-11-07 14:48:44'),
(8, 1, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 14:49:41'),
(9, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 15:02:49'),
(10, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 15:09:01'),
(11, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 15:14:05'),
(12, 1, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:22:50'),
(13, 1, 'ADMIN_ACTION', 'Admin admin deactivated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 15:23:10'),
(14, 1, 'ADMIN_ACTION', 'Admin admin deactivated user John (ID: 4)', '0:0:0:0:0:0:0:1', '2025-11-07 15:24:30'),
(15, 1, 'ADMIN_ACTION', 'Admin admin deactivated user demo (ID: 2)', '0:0:0:0:0:0:0:1', '2025-11-07 15:24:32'),
(16, 1, 'ADMIN_ACTION', 'Admin admin activated user xuanquoc898 (ID: 3)', '0:0:0:0:0:0:0:1', '2025-11-07 15:25:58'),
(17, 3, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:26:05'),
(18, 3, 'LOGOUT', 'User logged out', '0:0:0:0:0:0:0:1', '2025-11-07 15:28:54'),
(19, 5, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:29:45'),
(20, 1, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:42:03'),
(21, 1, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:45:56'),
(22, 5, 'LOGIN', 'User logged in successfully', '0:0:0:0:0:0:0:1', '2025-11-07 15:48:47');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `conversion_jobs`
--

CREATE TABLE `conversion_jobs` (
  `job_id` int(11) NOT NULL,
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` enum('PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'PENDING',
  `progress` int(11) DEFAULT 0,
  `priority` int(11) DEFAULT 0,
  `output_format` varchar(20) NOT NULL,
  `output_resolution` varchar(20) DEFAULT NULL,
  `quality` varchar(20) DEFAULT NULL,
  `video_bitrate` varchar(20) DEFAULT NULL,
  `audio_bitrate` varchar(20) DEFAULT NULL,
  `codec` varchar(20) DEFAULT 'libx264',
  `frame_rate` int(11) DEFAULT 30,
  `start_time` int(11) DEFAULT NULL,
  `end_time` int(11) DEFAULT NULL,
  `output_filename` varchar(255) DEFAULT NULL,
  `output_path` varchar(500) DEFAULT NULL,
  `output_size` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `started_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `estimated_time` int(11) DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  `retry_count` int(11) DEFAULT 0,
  `max_retries` int(11) DEFAULT 3
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `conversion_jobs`
--

INSERT INTO `conversion_jobs` (`job_id`, `video_id`, `user_id`, `status`, `progress`, `priority`, `output_format`, `output_resolution`, `quality`, `video_bitrate`, `audio_bitrate`, `codec`, `frame_rate`, `start_time`, `end_time`, `output_filename`, `output_path`, `output_size`, `created_at`, `started_at`, `completed_at`, `estimated_time`, `error_message`, `retry_count`, `max_retries`) VALUES
(11, 11, 1, 'COMPLETED', 100, 0, 'mp4', '3840x2160', 'high', '2500k', '192k', 'libx264', 30, NULL, NULL, '7385122-uhd_3840_2160_30fps_converted_1762522820815.mp4', 'D:\\apache-tomcat-10.1.48\\webapps\\videoconverter\\converted\\7385122-uhd_3840_2160_30fps_converted_1762522820815.mp4', 10523238, '2025-11-07 13:40:20', '2025-11-07 06:40:20', '2025-11-07 06:41:12', 6, NULL, 0, 3);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT 'default-avatar.png',
  `role` enum('USER','ADMIN') DEFAULT 'USER',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `daily_quota` int(11) DEFAULT 5,
  `total_conversions` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `password`, `full_name`, `phone`, `avatar`, `role`, `created_at`, `last_login`, `is_active`, `daily_quota`, `total_conversions`) VALUES
(1, 'admin', 'admin@example.com', '$2a$10$ATyAYsoDq8kjD9TNtuDD3eozks7b.XcDOUX03kVZMzsdidwJ6NNG2', 'Administrator', '1234', 'images/default-avatar.png', 'ADMIN', '2025-11-07 08:54:32', '2025-11-07 15:45:56', 1, 5, 2),
(2, 'demo', 'demo@example.com', '$2a$10$ObFPVKwziMLrJRD7r4nVAOE1osy18RsUfu7NNxW6Srx2vs3cKvI6.', 'Demo User', NULL, 'images/default-avatar.png', 'USER', '2025-11-07 08:54:32', '2025-11-07 13:14:43', 0, 5, 1),
(3, 'xuanquoc898', 'to.met.roi898@gmail.com', '$2a$10$W.w6ldK1ixpbpaglJ0wjOenQQuRWeqKH81ZB6KmxnfTXtBCIEul0i', 'Spade', '', 'images/default-avatar.png', 'USER', '2025-11-07 09:54:05', '2025-11-07 15:26:05', 1, 5, 3),
(4, 'John', 'huongtuctrung@gmail.com', '$2a$10$D067liQh7mF.WIZVdz3QTu5i9bAhz4ZCePJ9llwPgnNuQraHU6x4K', 'John', '', 'images/default-avatar.png', 'USER', '2025-11-07 10:30:54', '2025-11-07 10:31:38', 0, 5, 0),
(5, 'haha', 'haha@gmail.com', '$2a$10$TiWyeEEj1ky./oEaRbdFDu/WJwONENPmPptPQ9kXkpAcdOVJ3OuGW', 'John', '', 'uploads/avatars/avatar_5_1762530541861.jpg', 'USER', '2025-11-07 15:29:38', '2025-11-07 15:48:47', 1, 5, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `videos`
--

CREATE TABLE `videos` (
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint(20) NOT NULL,
  `duration` int(11) DEFAULT NULL,
  `resolution` varchar(20) DEFAULT NULL,
  `format` varchar(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `videos`
--

INSERT INTO `videos` (`video_id`, `user_id`, `original_filename`, `file_path`, `file_size`, `duration`, `resolution`, `format`, `uploaded_at`) VALUES
(11, 1, '7385122-uhd_3840_2160_30fps.mp4', 'D:\\apache-tomcat-10.1.48\\webapps\\videoconverter\\uploads\\7385122-uhd_3840_2160_30fps_1762522820088.mp4', 16795727, 6, '3840x2160', 'mp4', '2025-11-07 13:40:20');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Chỉ mục cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  ADD PRIMARY KEY (`job_id`),
  ADD KEY `video_id` (`video_id`),
  ADD KEY `idx_user_jobs` (`user_id`,`status`),
  ADD KEY `idx_job_status` (`status`,`created_at`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_user_email` (`email`),
  ADD KEY `idx_user_username` (`username`);

--
-- Chỉ mục cho bảng `videos`
--
ALTER TABLE `videos`
  ADD PRIMARY KEY (`video_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  MODIFY `job_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `videos`
--
ALTER TABLE `videos`
  MODIFY `video_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD CONSTRAINT `activity_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  ADD CONSTRAINT `conversion_jobs_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `conversion_jobs_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `videos`
--
ALTER TABLE `videos`
  ADD CONSTRAINT `videos_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
