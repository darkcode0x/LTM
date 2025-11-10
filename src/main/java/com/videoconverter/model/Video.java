package com.videoconverter.model;

import java.sql.Timestamp;

/**
 * Video Model - Represents videos table in database
 */
public class Video {
    private int videoId;
    private int userId;
    private String originalFilename;
    private String filePath;
    private long fileSize;
    private int duration;           // in seconds
    private String resolution;      // e.g., "1920x1080"
    private String format;          // e.g., "mp4", "avi"
    private Timestamp uploadedAt;

    // Default constructor
    public Video() {
    }

    // Constructor without ID (for new video upload)
    public Video(int userId, String originalFilename, String filePath, long fileSize) {
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // Constructor with metadata
    public Video(int userId, String originalFilename, String filePath, long fileSize, 
                 int duration, String resolution, String format) {
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.duration = duration;
        this.resolution = resolution;
        this.format = format;
    }

    // Full constructor
    public Video(int videoId, int userId, String originalFilename, String filePath, 
                 long fileSize, int duration, String resolution, String format, 
                 Timestamp uploadedAt) {
        this.videoId = videoId;
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.duration = duration;
        this.resolution = resolution;
        this.format = format;
        this.uploadedAt = uploadedAt;
    }

    // Getters and Setters
    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    /**
     * Get file size in human-readable format
     */
    public String getFileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get duration in human-readable format (HH:MM:SS)
     */
    public String getDurationFormatted() {
        if (duration <= 0) {
            return "00:00";
        }
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoId=" + videoId +
                ", userId=" + userId +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileSize=" + getFileSizeFormatted() +
                ", duration=" + getDurationFormatted() +
                ", resolution='" + resolution + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
