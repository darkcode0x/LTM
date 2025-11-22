package com.videoconverter.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FFmpegWrapper - Simplified wrapper for FFmpeg video conversion
 */
public class FFmpegWrapper {
    private static final String FFMPEG_CMD = "ffmpeg";
    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})");
    private static final Pattern TIME_PATTERN = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})");

    /**
     * Convert video to specified format with progress callback
     */
    public boolean convertVideo(String inputPath, String outputPath, String format, Consumer<Integer> progressCallback) {
        try {
            // Build FFmpeg command
            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_CMD,
                "-i", inputPath,
                "-y", // Overwrite output
                outputPath
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read output to track progress
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int totalDuration = 0;

            while ((line = reader.readLine()) != null) {
                // Get total duration
                if (totalDuration == 0) {
                    Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                    if (durationMatcher.find()) {
                        int hours = Integer.parseInt(durationMatcher.group(1));
                        int minutes = Integer.parseInt(durationMatcher.group(2));
                        int seconds = Integer.parseInt(durationMatcher.group(3));
                        totalDuration = hours * 3600 + minutes * 60 + seconds;
                    }
                }

                // Track progress
                Matcher timeMatcher = TIME_PATTERN.matcher(line);
                if (timeMatcher.find() && totalDuration > 0) {
                    int hours = Integer.parseInt(timeMatcher.group(1));
                    int minutes = Integer.parseInt(timeMatcher.group(2));
                    int seconds = Integer.parseInt(timeMatcher.group(3));
                    int currentTime = hours * 3600 + minutes * 60 + seconds;
                    int progress = Math.min(100, (int) ((currentTime * 100.0) / totalDuration));

                    if (progressCallback != null) {
                        progressCallback.accept(progress);
                    }
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if FFmpeg is available
     */
    public static boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(FFMPEG_CMD, "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

