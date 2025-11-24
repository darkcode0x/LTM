package com.videoconverter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper để gọi FFmpeg convert video
 *
 * Chức năng:
 * - Convert video sang format khác (mp4, avi, mkv, mov, webm)
 * - Track progress (0-100%)
 * - Timeout 30 phút
 */
public class FFmpegWrapper {
    private static final String FFMPEG_CMD = "ffmpeg";
    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})");
    private static final Pattern TIME_PATTERN = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})");
    private static final int TIMEOUT_MINUTES = 30;

    /**
     * Convert video sang format mới
     *
     * @param inputPath đường dẫn video gốc
     * @param outputPath đường dẫn video output
     * @param format format mới (mp4, avi, mkv, mov, webm)
     * @param progressCallback callback nhận progress (0-100%)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean convertVideo(String inputPath, String outputPath, String format,
                                Consumer<Integer> progressCallback) {
        // Validate inputs
        if (inputPath == null || outputPath == null || format == null) {
            return false;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            return false;
        }

        Process process = null;
        try {
            // Build FFmpeg command
            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_CMD,
                "-i", inputPath,
                "-f", format,
                "-y",
                outputPath
            );
            pb.redirectErrorStream(true);

            process = pb.start();

            // Đọc output và track progress
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                int totalDuration = 0;

                while ((line = reader.readLine()) != null) {
                    // Lấy tổng thời lượng video
                    if (totalDuration == 0) {
                        Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                        if (durationMatcher.find()) {
                            totalDuration = parseTime(durationMatcher);
                        }
                    }

                    // Track progress hiện tại
                    Matcher timeMatcher = TIME_PATTERN.matcher(line);
                    if (timeMatcher.find() && totalDuration > 0) {
                        int currentTime = parseTime(timeMatcher);
                        int progress = Math.min(100, (currentTime * 100) / totalDuration);

                        if (progressCallback != null) {
                            progressCallback.accept(progress);
                        }
                    }
                }
            }

            // Wait với timeout
            boolean finished = process.waitFor(TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;

        } catch (Exception e) {
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Parse time từ regex matcher (HH:MM:SS → seconds)
     */
    private int parseTime(Matcher matcher) {
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));
        return hours * 3600 + minutes * 60 + seconds;
    }
}


