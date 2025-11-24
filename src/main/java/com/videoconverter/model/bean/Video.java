package com.videoconverter.model.bean;

import java.sql.Timestamp;

/**
 * Video Entity - Represents videos table
 */
public class Video {
    private int videoId;
    private int userId;
    private String filename;
    private String filePath;
    private long fileSize;
    private Timestamp uploadedAt;

    public Video() {
    }

    public Video(int userId, String filename, String filePath, long fileSize) {
        this.userId = userId;
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public Video(int videoId, int userId, String filename, String filePath, long fileSize, Timestamp uploadedAt) {
        this.videoId = videoId;
        this.userId = userId;
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getFormattedFileSize() {
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoId=" + videoId +
                ", userId=" + userId +
                ", filename='" + filename + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}

