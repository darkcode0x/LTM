package com.videoconverter.model;

import java.sql.Timestamp;

/**
 * ConversionJob Model - Represents conversion_jobs table in database
 */
public class ConversionJob {
    // Basic info
    private int jobId;
    private int videoId;
    private int userId;
    
    // Status
    private JobStatus status;
    private int progress;           // 0-100%
    private int priority;           // higher = more priority
    
    // Conversion settings
    private String outputFormat;
    private String outputResolution;
    private String quality;
    private String videoBitrate;
    private String audioBitrate;
    private String codec;
    private int frameRate;
    
    // Trimming (optional)
    private Integer startTime;      // in seconds
    private Integer endTime;        // in seconds
    
    // Output result
    private String outputFilename;
    private String outputPath;
    private Long outputSize;
    
    // Timestamps
    private Timestamp createdAt;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private Integer estimatedTime;  // estimated time in seconds
    
    // Error handling
    private String errorMessage;
    private int retryCount;
    private int maxRetries;

    // Enum for job status
    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    // Default constructor
    public ConversionJob() {
        this.status = JobStatus.PENDING;
        this.progress = 0;
        this.priority = 0;
        this.codec = "libx264";
        this.frameRate = 30;
        this.retryCount = 0;
        this.maxRetries = 3;
    }

    // Constructor with basic info
    public ConversionJob(int videoId, int userId, String outputFormat) {
        this();
        this.videoId = videoId;
        this.userId = userId;
        this.outputFormat = outputFormat;
    }

    // Constructor with settings
    public ConversionJob(int videoId, int userId, ConversionSettings settings) {
        this();
        this.videoId = videoId;
        this.userId = userId;
        this.outputFormat = settings.getOutputFormat();
        this.outputResolution = settings.getOutputResolution();
        this.quality = settings.getQuality();
        this.videoBitrate = settings.getVideoBitrate();
        this.audioBitrate = settings.getAudioBitrate();
        this.codec = settings.getCodec();
        this.frameRate = settings.getFrameRate();
        this.startTime = settings.getStartTime();
        this.endTime = settings.getEndTime();
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getStatusString() {
        return status != null ? status.name() : "PENDING";
    }

    public void setStatusFromString(String status) {
        this.status = JobStatus.valueOf(status.toUpperCase());
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress)); // Clamp between 0-100
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getOutputResolution() {
        return outputResolution;
    }

    public void setOutputResolution(String outputResolution) {
        this.outputResolution = outputResolution;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getVideoBitrate() {
        return videoBitrate;
    }

    public void setVideoBitrate(String videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public String getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(String audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Long getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(Long outputSize) {
        this.outputSize = outputSize;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    // Utility methods
    
    /**
     * Check if job can be retried
     */
    public boolean canRetry() {
        return status == JobStatus.FAILED && retryCount < maxRetries;
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * Check if trimming is enabled
     */
    public boolean isTrimEnabled() {
        return startTime != null || endTime != null;
    }

    /**
     * Get ConversionSettings object from this job
     */
    public ConversionSettings getConversionSettings() {
        ConversionSettings settings = new ConversionSettings();
        settings.setOutputFormat(outputFormat);
        settings.setOutputResolution(outputResolution);
        settings.setQuality(quality);
        settings.setVideoBitrate(videoBitrate);
        settings.setAudioBitrate(audioBitrate);
        settings.setCodec(codec);
        settings.setFrameRate(frameRate);
        settings.setStartTime(startTime);
        settings.setEndTime(endTime);
        return settings;
    }

    /**
     * Get processing time in seconds (if completed)
     */
    public Long getProcessingTimeSeconds() {
        if (startedAt != null && completedAt != null) {
            return (completedAt.getTime() - startedAt.getTime()) / 1000;
        }
        return null;
    }

    /**
     * Get formatted output file size
     */
    public String getOutputSizeFormatted() {
        if (outputSize == null) {
            return "N/A";
        }
        if (outputSize < 1024) {
            return outputSize + " B";
        } else if (outputSize < 1024 * 1024) {
            return String.format("%.2f KB", outputSize / 1024.0);
        } else if (outputSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", outputSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", outputSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "ConversionJob{" +
                "jobId=" + jobId +
                ", videoId=" + videoId +
                ", userId=" + userId +
                ", status=" + status +
                ", progress=" + progress + "%" +
                ", outputFormat='" + outputFormat + '\'' +
                ", quality='" + quality + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}
