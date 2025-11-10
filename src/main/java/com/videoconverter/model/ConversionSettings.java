package com.videoconverter.model;

/**
 * ConversionSettings Model - Represents conversion configuration settings
 * This class encapsulates all settings needed for video conversion
 */
public class ConversionSettings {
    private String outputFormat;        // mp4, avi, mkv, webm, etc.
    private String outputResolution;    // 1920x1080, 1280x720, 854x480, etc.
    private String quality;             // high, medium, low
    private String videoBitrate;        // 2000k, 1500k, 1000k, etc.
    private String audioBitrate;        // 192k, 128k, 96k, etc.
    private String codec;               // libx264, libx265, libvpx, etc.
    private int frameRate;              // 30, 60, 24, etc.
    private Integer startTime;          // start time in seconds (for trimming)
    private Integer endTime;            // end time in seconds (for trimming)

    // Default constructor
    public ConversionSettings() {
        this.codec = "libx264";
        this.frameRate = 30;
        this.quality = "medium";
    }

    // Constructor with essential settings
    public ConversionSettings(String outputFormat, String outputResolution, String quality) {
        this();
        this.outputFormat = outputFormat;
        this.outputResolution = outputResolution;
        this.quality = quality;
    }

    // Full constructor
    public ConversionSettings(String outputFormat, String outputResolution, String quality,
                            String videoBitrate, String audioBitrate, String codec, 
                            int frameRate, Integer startTime, Integer endTime) {
        this.outputFormat = outputFormat;
        this.outputResolution = outputResolution;
        this.quality = quality;
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.codec = codec;
        this.frameRate = frameRate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
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

    /**
     * Check if video trimming is enabled
     */
    public boolean isTrimEnabled() {
        return startTime != null || endTime != null;
    }

    /**
     * Get trim duration in seconds
     */
    public Integer getTrimDuration() {
        if (startTime != null && endTime != null) {
            return endTime - startTime;
        }
        return null;
    }

    /**
     * Apply quality preset (sets bitrate based on quality level)
     */
    public void applyQualityPreset() {
        switch (quality.toLowerCase()) {
            case "high":
                this.videoBitrate = "2500k";
                this.audioBitrate = "192k";
                break;
            case "medium":
                this.videoBitrate = "1500k";
                this.audioBitrate = "128k";
                break;
            case "low":
                this.videoBitrate = "800k";
                this.audioBitrate = "96k";
                break;
            default:
                this.videoBitrate = "1500k";
                this.audioBitrate = "128k";
        }
    }

    /**
     * Validate settings
     */
    public boolean isValid() {
        if (outputFormat == null || outputFormat.trim().isEmpty()) {
            return false;
        }
        if (startTime != null && endTime != null && startTime >= endTime) {
            return false;
        }
        if (frameRate <= 0 || frameRate > 120) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConversionSettings{" +
                "outputFormat='" + outputFormat + '\'' +
                ", outputResolution='" + outputResolution + '\'' +
                ", quality='" + quality + '\'' +
                ", videoBitrate='" + videoBitrate + '\'' +
                ", audioBitrate='" + audioBitrate + '\'' +
                ", codec='" + codec + '\'' +
                ", frameRate=" + frameRate +
                ", trimEnabled=" + isTrimEnabled() +
                '}';
    }
}
