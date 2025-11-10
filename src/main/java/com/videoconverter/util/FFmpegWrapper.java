package com.videoconverter.util;

import com.videoconverter.model.ConversionSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FFmpegWrapper - Utility class for video conversion using FFmpeg
 * Provides methods to convert videos and extract video information
 */
public class FFmpegWrapper {

    // FFmpeg executable path (configure based on your system)
    private static final String FFMPEG_PATH = "ffmpeg";
    private static final String FFPROBE_PATH = "ffprobe";
    
    // Regex patterns for parsing FFmpeg output
    private static final Pattern TIME_PATTERN = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(\\d{3,4})x(\\d{3,4})");

    /**
     * ProgressListener interface for conversion progress callback
     */
    public interface ProgressListener {
        /**
         * Called when progress is updated
         * 
         * @param progress Progress percentage (0-100)
         * @param timeProcessed Time processed in seconds
         * @param message Status message
         */
        void onProgress(int progress, int timeProcessed, String message);
        
        /**
         * Called when conversion is completed
         */
        void onComplete();
        
        /**
         * Called when an error occurs
         * 
         * @param errorMessage Error message
         */
        void onError(String errorMessage);
    }

    /**
     * VideoInfo class to hold video metadata
     */
    public static class VideoInfo {
        private int duration;        // in seconds
        private int width;
        private int height;
        private String resolution;   // WxH
        private String format;
        private String codec;
        private double frameRate;
        private long bitrate;
        
        public VideoInfo() {}
        
        // Getters and Setters
        public int getDuration() {
            return duration;
        }
        
        public void setDuration(int duration) {
            this.duration = duration;
        }
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public String getResolution() {
            if (resolution == null && width > 0 && height > 0) {
                return width + "x" + height;
            }
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
        
        public String getCodec() {
            return codec;
        }
        
        public void setCodec(String codec) {
            this.codec = codec;
        }
        
        public double getFrameRate() {
            return frameRate;
        }
        
        public void setFrameRate(double frameRate) {
            this.frameRate = frameRate;
        }
        
        public long getBitrate() {
            return bitrate;
        }
        
        public void setBitrate(long bitrate) {
            this.bitrate = bitrate;
        }
        
        @Override
        public String toString() {
            return "VideoInfo{" +
                    "duration=" + duration + "s" +
                    ", resolution=" + getResolution() +
                    ", format='" + format + '\'' +
                    ", codec='" + codec + '\'' +
                    ", frameRate=" + frameRate +
                    ", bitrate=" + bitrate +
                    '}';
        }
    }

    /**
     * Convert video with specified settings
     * 
     * @param inputPath Input video file path
     * @param outputPath Output video file path
     * @param settings Conversion settings
     * @param listener Progress listener (can be null)
     * @return true if conversion successful, false otherwise
     */
    public static boolean convertVideo(String inputPath, String outputPath, 
                                      ConversionSettings settings, 
                                      ProgressListener listener) {
        Process process = null;
        BufferedReader errorReader = null;
        BufferedReader outputReader = null;
        
        try {
            // Validate input file
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                String error = "Input file does not exist: " + inputPath;
                System.err.println(error);
                if (listener != null) listener.onError(error);
                return false;
            }
            
            // Get video duration for progress calculation
            VideoInfo videoInfo = getVideoInfo(inputPath);
            int totalDuration = videoInfo != null ? videoInfo.getDuration() : 0;
            
            // Build FFmpeg command
            List<String> command = buildFFmpegCommand(inputPath, outputPath, settings);
            
            System.out.println("Executing FFmpeg command: " + String.join(" ", command));
            
            // Create process builder with separate error and output streams
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false); // Keep streams separate to avoid deadlock
            
            // Start process
            process = pb.start();
            final Process finalProcess = process;
            
            // Create thread to consume stdout (prevents buffer deadlock)
            Thread outputConsumer = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("FFmpeg stdout: " + line);
                    }
                } catch (Exception e) {
                    // Ignore exceptions during stream reading
                }
            });
            outputConsumer.setDaemon(true);
            outputConsumer.start();
            
            // Read stderr in main thread (FFmpeg outputs progress to stderr)
            errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
            );
            
            // Variables for timeout detection
            long lastProgressTime = System.currentTimeMillis();
            final long TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes without progress
            int lastProgress = -1;
            
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println("FFmpeg: " + line);
                
                // Parse progress from time information
                if (listener != null && totalDuration > 0) {
                    Matcher matcher = TIME_PATTERN.matcher(line);
                    if (matcher.find()) {
                        int hours = Integer.parseInt(matcher.group(1));
                        int minutes = Integer.parseInt(matcher.group(2));
                        int seconds = Integer.parseInt(matcher.group(3));
                        
                        int timeProcessed = hours * 3600 + minutes * 60 + seconds;
                        int progress = Math.min(100, (int) ((timeProcessed * 100.0) / totalDuration));
                        
                        // Update progress timestamp if progress changed
                        if (progress != lastProgress) {
                            lastProgressTime = System.currentTimeMillis();
                            lastProgress = progress;
                        }
                        
                        listener.onProgress(progress, timeProcessed, 
                            "Converting... " + progress + "%");
                    }
                }
                
                // Check for timeout (no progress for too long)
                if (System.currentTimeMillis() - lastProgressTime > TIMEOUT_MS) {
                    System.err.println("FFmpeg timeout: No progress for " + (TIMEOUT_MS / 1000) + " seconds");
                    process.destroy();
                    if (listener != null) {
                        listener.onError("Conversion timeout: Process appears to be stuck");
                    }
                    return false;
                }
            }
            
            // Wait for process to complete with timeout
            boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!finished) {
                System.err.println("FFmpeg process did not complete in time, forcing termination");
                process.destroyForcibly();
                if (listener != null) {
                    listener.onError("Conversion timeout: Process forced to terminate");
                }
                return false;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                System.out.println("Conversion completed successfully!");
                if (listener != null) listener.onComplete();
                return true;
            } else {
                String error = "FFmpeg exited with code: " + exitCode;
                System.err.println(error);
                if (listener != null) listener.onError(error);
                return false;
            }
            
        } catch (Exception e) {
            String error = "Error during video conversion: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            
            // Clean up process if still running
            if (process != null && process.isAlive()) {
                System.err.println("Killing stuck FFmpeg process...");
                process.destroyForcibly();
            }
            
            if (listener != null) listener.onError(error);
            return false;
        } finally {
            // Clean up resources
            try {
                if (errorReader != null) errorReader.close();
                if (outputReader != null) outputReader.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            
            // Final safety check - ensure process is terminated
            if (process != null && process.isAlive()) {
                System.err.println("Force killing FFmpeg process in finally block");
                process.destroyForcibly();
            }
        }
    }

    /**
     * Get video information using ffprobe
     * 
     * @param videoPath Path to video file
     * @return VideoInfo object with video metadata, or null if failed
     */
    public static VideoInfo getVideoInfo(String videoPath) {
        VideoInfo info = new VideoInfo();
        
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                System.err.println("Video file does not exist: " + videoPath);
                return null;
            }
            
            // Build ffprobe command
            List<String> command = new ArrayList<>();
            command.add(FFPROBE_PATH);
            command.add("-v");
            command.add("error");
            command.add("-show_entries");
            command.add("format=duration,bit_rate:stream=width,height,codec_name,r_frame_rate");
            command.add("-of");
            command.add("default=noprint_wrappers=1");
            command.add(videoPath);
            
            System.out.println("Executing ffprobe command: " + String.join(" ", command));
            
            // Execute command
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            Map<String, String> properties = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("ffprobe: " + line);
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        properties.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("ffprobe exited with code: " + exitCode);
                return null;
            }
            
            // Parse properties
            if (properties.containsKey("width")) {
                info.setWidth(Integer.parseInt(properties.get("width")));
            }
            
            if (properties.containsKey("height")) {
                info.setHeight(Integer.parseInt(properties.get("height")));
            }
            
            if (properties.containsKey("duration")) {
                double duration = Double.parseDouble(properties.get("duration"));
                info.setDuration((int) Math.round(duration));
            }
            
            if (properties.containsKey("codec_name")) {
                info.setCodec(properties.get("codec_name"));
            }
            
            if (properties.containsKey("bit_rate")) {
                info.setBitrate(Long.parseLong(properties.get("bit_rate")));
            }
            
            if (properties.containsKey("r_frame_rate")) {
                String frameRateStr = properties.get("r_frame_rate");
                if (frameRateStr.contains("/")) {
                    String[] parts = frameRateStr.split("/");
                    if (parts.length == 2) {
                        double num = Double.parseDouble(parts[0]);
                        double den = Double.parseDouble(parts[1]);
                        info.setFrameRate(num / den);
                    }
                } else {
                    info.setFrameRate(Double.parseDouble(frameRateStr));
                }
            }
            
            // Set resolution string
            if (info.getWidth() > 0 && info.getHeight() > 0) {
                info.setResolution(info.getWidth() + "x" + info.getHeight());
            }
            
            // Try to get format from file extension
            String fileName = videoFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                info.setFormat(fileName.substring(dotIndex + 1));
            }
            
            System.out.println("Video info extracted: " + info);
            return info;
            
        } catch (Exception e) {
            System.err.println("Error getting video info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get video information using ffmpeg (alternative method)
     * 
     * @param videoPath Path to video file
     * @return VideoInfo object with video metadata, or null if failed
     */
    public static VideoInfo getVideoInfoFFmpeg(String videoPath) {
        VideoInfo info = new VideoInfo();
        
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                System.err.println("Video file does not exist: " + videoPath);
                return null;
            }
            
            // Build FFmpeg command to get video info
            List<String> command = new ArrayList<>();
            command.add(FFMPEG_PATH);
            command.add("-i");
            command.add(videoPath);
            command.add("-hide_banner");
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse duration
                Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                if (durationMatcher.find()) {
                    int hours = Integer.parseInt(durationMatcher.group(1));
                    int minutes = Integer.parseInt(durationMatcher.group(2));
                    int seconds = Integer.parseInt(durationMatcher.group(3));
                    info.setDuration(hours * 3600 + minutes * 60 + seconds);
                }
                
                // Parse resolution
                if (line.contains("Video:")) {
                    Matcher resMatcher = RESOLUTION_PATTERN.matcher(line);
                    if (resMatcher.find()) {
                        int width = Integer.parseInt(resMatcher.group(1));
                        int height = Integer.parseInt(resMatcher.group(2));
                        info.setWidth(width);
                        info.setHeight(height);
                        info.setResolution(width + "x" + height);
                    }
                }
            }
            
            process.waitFor();
            
            // Get format from file extension
            String fileName = videoFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                info.setFormat(fileName.substring(dotIndex + 1));
            }
            
            return info;
            
        } catch (Exception e) {
            System.err.println("Error getting video info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build FFmpeg command based on conversion settings
     * 
     * @param inputPath Input file path
     * @param outputPath Output file path
     * @param settings Conversion settings
     * @return List of command arguments
     */
    private static List<String> buildFFmpegCommand(String inputPath, String outputPath, 
                                                   ConversionSettings settings) {
        List<String> command = new ArrayList<>();
        
        // Base command
        command.add(FFMPEG_PATH);
        command.add("-i");
        command.add(inputPath);
        
        // Determine video codec based on output format
        String videoCodec = getVideoCodecForFormat(settings.getOutputFormat(), settings.getCodec());
        String audioCodec = getAudioCodecForFormat(settings.getOutputFormat());
        
        // Video codec
        command.add("-c:v");
        command.add(videoCodec);
        
        // Add encoding preset for performance (prevents hanging on heavy tasks)
        if (videoCodec.equals("libx264") || videoCodec.equals("libx265")) {
            command.add("-preset");
            command.add("medium"); // Balance between speed and quality (faster, fast, medium, slow, slower)
            command.add("-crf");
            command.add("23"); // Constant Rate Factor (18-28, lower = better quality)
        } else if (videoCodec.equals("libvpx-vp9")) {
            command.add("-deadline");
            command.add("good"); // good, best, realtime
            command.add("-cpu-used");
            command.add("2"); // 0-5, higher = faster but lower quality
        }
        
        // Limit threads to prevent system overload
        command.add("-threads");
        command.add("4"); // Limit to 4 threads
        
        // Audio codec
        command.add("-c:a");
        command.add(audioCodec);
        
        // Video bitrate
        if (settings.getVideoBitrate() != null && !settings.getVideoBitrate().isEmpty()) {
            command.add("-b:v");
            command.add(settings.getVideoBitrate());
        }
        
        // Audio bitrate
        if (settings.getAudioBitrate() != null && !settings.getAudioBitrate().isEmpty()) {
            command.add("-b:a");
            command.add(settings.getAudioBitrate());
        }
        
        // Add buffer size to prevent memory issues with large files
        command.add("-bufsize");
        command.add("2M");
        
        // Resolution (scale)
        if (settings.getOutputResolution() != null && !settings.getOutputResolution().isEmpty()) {
            command.add("-vf");
            command.add("scale=" + settings.getOutputResolution().replace("x", ":"));
        }
        
        // Frame rate
        if (settings.getFrameRate() > 0) {
            command.add("-r");
            command.add(String.valueOf(settings.getFrameRate()));
        }
        
        // Trimming (if enabled)
        if (settings.getStartTime() != null && settings.getStartTime() > 0) {
            command.add("-ss");
            command.add(String.valueOf(settings.getStartTime()));
        }
        
        if (settings.getEndTime() != null && settings.getEndTime() > 0) {
            command.add("-to");
            command.add(String.valueOf(settings.getEndTime()));
        }
        
        // Output format
        command.add("-f");
        command.add(settings.getOutputFormat());
        
        // Add format-specific optimizations to prevent hanging
        String format = settings.getOutputFormat().toLowerCase();
        if (format.equals("mp4") || format.equals("mov")) {
            // Fast start for MP4/MOV (write metadata at beginning)
            command.add("-movflags");
            command.add("+faststart");
        }
        
        // Set max muxing queue size to prevent deadlock
        command.add("-max_muxing_queue_size");
        command.add("1024");
        
        // Overwrite output file
        command.add("-y");
        
        // Progress output
        command.add("-progress");
        command.add("pipe:1");
        
        // Output file
        command.add(outputPath);
        
        return command;
    }
    
    /**
     * Get appropriate video codec for the output format
     * 
     * @param outputFormat Output video format
     * @param preferredCodec User's preferred codec (can be null)
     * @return Appropriate video codec
     */
    private static String getVideoCodecForFormat(String outputFormat, String preferredCodec) {
        // If user specified codec and it's compatible, use it
        if (preferredCodec != null && !preferredCodec.isEmpty()) {
            // Check compatibility
            if (isCodecCompatibleWithFormat(preferredCodec, outputFormat)) {
                return preferredCodec;
            }
        }
        
        // Auto-select codec based on format
        switch (outputFormat.toLowerCase()) {
            case "webm":
                return "libvpx-vp9";  // VP9 for WebM (better quality than VP8)
            case "mp4":
            case "m4v":
                return "libx264";     // H.264 for MP4
            case "mkv":
                return "libx264";     // H.264 is widely compatible
            case "avi":
                return "mpeg4";       // MPEG-4 for AVI
            case "mov":
                return "libx264";     // H.264 for MOV
            case "flv":
                return "flv1";        // FLV1 for Flash Video
            default:
                return "libx264";     // Default to H.264
        }
    }
    
    /**
     * Get appropriate audio codec for the output format
     * 
     * @param outputFormat Output video format
     * @return Appropriate audio codec
     */
    private static String getAudioCodecForFormat(String outputFormat) {
        switch (outputFormat.toLowerCase()) {
            case "webm":
                return "libopus";     // Opus for WebM (better than Vorbis)
            case "mp4":
            case "m4v":
            case "mov":
                return "aac";         // AAC for MP4/MOV
            case "mkv":
                return "aac";         // AAC is widely supported
            case "avi":
                return "mp3";         // MP3 for AVI
            case "flv":
                return "mp3";         // MP3 for FLV
            default:
                return "aac";         // Default to AAC
        }
    }
    
    /**
     * Check if video codec is compatible with output format
     * 
     * @param codec Video codec
     * @param format Output format
     * @return true if compatible, false otherwise
     */
    private static boolean isCodecCompatibleWithFormat(String codec, String format) {
        // WebM only supports VP8, VP9, AV1
        if (format.equalsIgnoreCase("webm")) {
            return codec.contains("vp8") || codec.contains("vp9") || 
                   codec.contains("av1") || codec.contains("libvpx");
        }
        
        // MP4 supports H.264, H.265, MPEG-4
        if (format.equalsIgnoreCase("mp4")) {
            return codec.contains("264") || codec.contains("265") || 
                   codec.contains("mpeg4") || codec.contains("h264") || codec.contains("h265");
        }
        
        // Other formats are generally flexible
        return true;
    }

    /**
     * Check if FFmpeg is available
     * 
     * @return true if FFmpeg is installed and accessible, false otherwise
     */
    public static boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(FFMPEG_PATH, "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("FFmpeg not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if ffprobe is available
     * 
     * @return true if ffprobe is installed and accessible, false otherwise
     */
    public static boolean isFFprobeAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(FFPROBE_PATH, "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("ffprobe not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get FFmpeg version
     * 
     * @return FFmpeg version string, or null if not available
     */
    public static String getFFmpegVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder(FFMPEG_PATH, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String firstLine = reader.readLine();
            process.waitFor();
            
            return firstLine;
        } catch (Exception e) {
            return null;
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        System.out.println("Testing FFmpegWrapper...");
        System.out.println("========================================");
        
        // Check availability
        System.out.println("FFmpeg available: " + isFFmpegAvailable());
        System.out.println("ffprobe available: " + isFFprobeAvailable());
        System.out.println("FFmpeg version: " + getFFmpegVersion());
        
        // Test with a sample video (update path as needed)
        String testVideoPath = "test_video.mp4";
        File testFile = new File(testVideoPath);
        
        if (testFile.exists()) {
            System.out.println("\nTesting getVideoInfo()...");
            VideoInfo info = getVideoInfo(testVideoPath);
            if (info != null) {
                System.out.println("✓ Video info: " + info);
            } else {
                System.out.println("✗ Failed to get video info");
            }
            
            // Test conversion
            System.out.println("\nTesting convertVideo()...");
            ConversionSettings settings = new ConversionSettings();
            settings.setOutputFormat("mp4");
            settings.setOutputResolution("1280x720");
            settings.setQuality("medium");
            settings.applyQualityPreset();
            
            String outputPath = "test_output.mp4";
            
            boolean success = convertVideo(testVideoPath, outputPath, settings, 
                new ProgressListener() {
                    @Override
                    public void onProgress(int progress, int timeProcessed, String message) {
                        System.out.println("Progress: " + progress + "% - " + message);
                    }
                    
                    @Override
                    public void onComplete() {
                        System.out.println("✓ Conversion completed!");
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        System.err.println("✗ Error: " + errorMessage);
                    }
                }
            );
            
            System.out.println("Conversion success: " + success);
        } else {
            System.out.println("\nTest video not found: " + testVideoPath);
            System.out.println("Please provide a test video to run conversion tests.");
        }
        
        System.out.println("\n========================================");
        System.out.println("Testing completed!");
    }
}
