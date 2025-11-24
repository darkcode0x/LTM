-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 24, 2025 lúc 06:11 AM
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
-- Cấu trúc bảng cho bảng `conversion_jobs`
--

CREATE TABLE `conversion_jobs` (
  `job_id` int(11) NOT NULL,
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `output_format` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `progress` int(11) NOT NULL DEFAULT 0,
  `output_path` varchar(500) DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `completed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Conversion job tracking';

--
-- Đang đổ dữ liệu cho bảng `conversion_jobs`
--

INSERT INTO `conversion_jobs` (`job_id`, `video_id`, `user_id`, `output_format`, `status`, `progress`, `output_path`, `error_message`, `created_at`, `completed_at`) VALUES
(1, 1, 3, 'mov', 'COMPLETED', 100, 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\uploads\\converted\\polyglotvidaa_converted.mov', NULL, '2025-11-22 11:19:39', '2025-11-22 11:19:41'),
(2, 2, 5, 'mp4', 'COMPLETED', 100, 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\uploads\\converted\\polyglotvidaa_converted_converted.mp4', NULL, '2025-11-23 13:32:58', '2025-11-23 13:33:02'),
(3, 3, 5, 'mov', 'COMPLETED', 100, 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\uploads\\converted\\polyglotvidaa_converted.mov', NULL, '2025-11-23 13:33:09', '2025-11-23 13:33:12'),
(4, 4, 5, 'mov', 'COMPLETED', 100, 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\uploads\\converted\\Kich (nhom Hoang Ha)_converted.mov', NULL, '2025-11-23 13:48:12', '2025-11-23 13:58:00'),
(5, 5, 5, 'mp4', 'COMPLETED', 100, 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\uploads\\converted\\polyglotvidaa_converted_converted.mp4', NULL, '2025-11-23 13:49:02', '2025-11-23 13:49:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `role` varchar(10) NOT NULL DEFAULT 'USER',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `role`, `created_at`) VALUES
(3, 'a222', '$2a$10$vytUUioq5/GzPkipOZkgju.LIoV0ZntoLK/63XLY1UVVLBk3Gme7S', 'a@gmail.com', 'USER', '2025-11-22 11:18:57'),
(4, 'admin', '$2a$10$2bndEtMdCIln1kTCMmncoOse.hJE.g/3xxJDr4.CHEUqGht4zTsoS', 'admin@videoconverter.com', 'ADMIN', '2025-11-22 11:26:44'),
(5, 'testuser', '$2a$10$PCM9BWWPqQqNzB67i0hTN.11PZcZVfLA8Sbjob8jAjoyhpfXlsjqC', 'user@test.com', 'USER', '2025-11-22 11:26:44');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `videos`
--

CREATE TABLE `videos` (
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint(20) NOT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Uploaded video metadata';

--
-- Đang đổ dữ liệu cho bảng `videos`
--

INSERT INTO `videos` (`video_id`, `user_id`, `filename`, `file_path`, `file_size`, `uploaded_at`) VALUES
(1, 3, 'polyglotvidaa.mp4', 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\\\uploads\\1763810378985_polyglotvidaa.mp4', 6300910, '2025-11-22 11:19:39'),
(2, 5, 'polyglotvidaa_converted.mov', 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\\\uploads\\1763904778682_polyglotvidaa_converted.mov', 800132, '2025-11-23 13:32:58'),
(3, 5, 'polyglotvidaa.mp4', 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\\\uploads\\1763904789801_polyglotvidaa.mp4', 6300910, '2025-11-23 13:33:09'),
(4, 5, 'Kich (nhom Hoang Ha).mp4', 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\\\uploads\\1763905690493_Kich (nhom Hoang Ha).mp4', 2350889475, '2025-11-23 13:48:12'),
(5, 5, 'polyglotvidaa_converted.mov', 'D:\\Desktop\\NHAP\\LTM_PHAMMINHTUAN\\cki\\LTM\\target\\video-converter-1.0-SNAPSHOT\\\\uploads\\1763905742487_polyglotvidaa_converted.mov', 800132, '2025-11-23 13:49:02');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  ADD PRIMARY KEY (`job_id`),
  ADD KEY `fk_job_video` (`video_id`),
  ADD KEY `fk_job_user` (`user_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Chỉ mục cho bảng `videos`
--
ALTER TABLE `videos`
  ADD PRIMARY KEY (`video_id`),
  ADD KEY `fk_video_user` (`user_id`),
  ADD KEY `idx_uploaded_at` (`uploaded_at`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  MODIFY `job_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `videos`
--
ALTER TABLE `videos`
  MODIFY `video_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `conversion_jobs`
--
ALTER TABLE `conversion_jobs`
  ADD CONSTRAINT `fk_job_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_job_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `videos`
--
ALTER TABLE `videos`
  ADD CONSTRAINT `fk_video_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
