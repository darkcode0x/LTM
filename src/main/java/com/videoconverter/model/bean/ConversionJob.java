package com.videoconverter.model.bean;

import java.sql.Timestamp;


public class ConversionJob {
    private int jobId;
    private int videoId;
    private int userId;
    private String outputFormat;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int progress; // 0-100
    private String outputPath;
    private String errorMessage;
    private Timestamp createdAt;
    private Timestamp completedAt;

    // For display purposes
    private String videoFilename;

    public ConversionJob() {
    }

    public ConversionJob(int videoId, int userId, String outputFormat) {
        this.videoId = videoId;
        this.userId = userId;
        this.outputFormat = outputFormat;
        this.status = "PENDING";
        this.progress = 0;
    }

    public ConversionJob(int jobId, int videoId, int userId, String outputFormat, String status,
                        int progress, String outputPath, String errorMessage,
                        Timestamp createdAt, Timestamp completedAt) {
        this.jobId = jobId;
        this.videoId = videoId;
        this.userId = userId;
        this.outputFormat = outputFormat;
        this.status = status;
        this.progress = progress;
        this.outputPath = outputPath;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

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

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getVideoFilename() {
        return videoFilename;
    }

    public void setVideoFilename(String videoFilename) {
        this.videoFilename = videoFilename;
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case "COMPLETED": return "bg-success";
            case "PROCESSING": return "bg-primary";
            case "PENDING": return "bg-warning";
            case "FAILED": return "bg-danger";
            default: return "bg-secondary";
        }
    }

    @Override
    public String toString() {
        return "ConversionJob{" +
                "jobId=" + jobId +
                ", videoId=" + videoId +
                ", outputFormat='" + outputFormat + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                '}';
    }
}

