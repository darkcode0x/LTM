package com.videoconverter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegWrapper {
    private static final String FFMPEG_CMD = "ffmpeg";
    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})");
    private static final Pattern TIME_PATTERN = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})");
    private static final int TIMEOUT_MINUTES = 45;

    public boolean convertVideo(String inputPath, String outputPath, String format,
                                Consumer<Integer> progressCallback) {
        if (inputPath == null || outputPath == null || format == null) {
            return false;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            return false;
        }

        boolean gpuSuccess = convertWithGPU(inputPath, outputPath, format, progressCallback);
        if (gpuSuccess) {
            return true;
        }

        return convertWithCPU(inputPath, outputPath, format, progressCallback);
    }

    private boolean convertWithGPU(String inputPath, String outputPath, String format,
                                   Consumer<Integer> progressCallback) {
        Process process = null;
        try {
            ProcessBuilder pb;

            if ("webm".equalsIgnoreCase(format)) {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-hwaccel", "cuda",
                    "-i", inputPath,
                    "-c:v", "libvpx-vp9",
                    "-b:v", "2M",
                    "-crf", "30",
                    "-c:a", "libopus",
                    "-b:a", "128k",
                    "-f", "webm",
                    "-y",
                    outputPath
                );
            } else if ("mkv".equalsIgnoreCase(format)) {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-hwaccel", "cuda",
                    "-hwaccel_output_format", "cuda",
                    "-extra_hw_frames", "8",
                    "-i", inputPath,
                    "-c:v", "hevc_nvenc",
                    "-preset", "p4",
                    "-rc", "vbr",
                    "-cq", "23",
                    "-b:v", "6M",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-f", "matroska",
                    "-y",
                    outputPath
                );
            } else {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-hwaccel", "cuda",
                    "-hwaccel_output_format", "cuda",
                    "-extra_hw_frames", "8",
                    "-i", inputPath,
                    "-c:v", "h264_nvenc",
                    "-preset", "p4",
                    "-rc", "vbr",
                    "-cq", "23",
                    "-b:v", "6M",
                    "-maxrate", "8M",
                    "-bufsize", "16M",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-f", format,
                    "-y",
                    outputPath
                );
            }
            pb.redirectErrorStream(true);

            process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                int totalDuration = 0;

                while ((line = reader.readLine()) != null) {
                    if (totalDuration == 0) {
                        Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                        if (durationMatcher.find()) {
                            totalDuration = parseTime(durationMatcher);
                        }
                    }

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

    private boolean convertWithCPU(String inputPath, String outputPath, String format,
                                   Consumer<Integer> progressCallback) {
        Process process = null;
        try {
            ProcessBuilder pb;

            if ("webm".equalsIgnoreCase(format)) {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-i", inputPath,
                    "-c:v", "libvpx-vp9",
                    "-b:v", "2M",
                    "-crf", "30",
                    "-speed", "4",
                    "-threads", "0",
                    "-c:a", "libopus",
                    "-b:a", "128k",
                    "-f", "webm",
                    "-y",
                    outputPath
                );
            } else if ("mkv".equalsIgnoreCase(format)) {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-crf", "28",
                    "-threads", "0",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-f", "matroska",
                    "-y",
                    outputPath
                );
            } else {
                pb = new ProcessBuilder(
                    FFMPEG_CMD,
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-crf", "28",
                    "-threads", "0",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-f", format,
                    "-y",
                    outputPath
                );
            }
            pb.redirectErrorStream(true);

            process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                int totalDuration = 0;

                while ((line = reader.readLine()) != null) {
                    if (totalDuration == 0) {
                        Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                        if (durationMatcher.find()) {
                            totalDuration = parseTime(durationMatcher);
                        }
                    }

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

    private int parseTime(Matcher matcher) {
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));
        return hours * 3600 + minutes * 60 + seconds;
    }
}


