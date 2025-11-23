package com.videoconverter.model.bo;

import com.videoconverter.model.bean.ConversionJob;
import com.videoconverter.model.bean.Video;
import com.videoconverter.model.dao.ConversionJobDAO;
import com.videoconverter.model.dao.VideoDAO;
import com.videoconverter.util.FFmpegWrapper;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ConversionBO - Trung tâm xử lý chuyển đổi video
 *
 * Chức năng:
 * - Quản lý hàng đợi (Queue) tối đa 100 jobs
 * - Quản lý 2 worker threads xử lý video song song
 * - Submit/delete/get jobs của user
 * - Tự động xử lý video background, không block user
 */
public class ConversionBO {
    private static volatile ConversionBO instance;

    private final VideoDAO videoDAO;
    private final ConversionJobDAO jobDAO;
    private final FFmpegWrapper ffmpegWrapper;
    private final BlockingQueue<ConversionJob> jobQueue;
    private final ExecutorService executorService;

    private static final int WORKER_COUNT = 2;
    private static final int MAX_QUEUE_SIZE = 100;
    private volatile boolean isRunning = false;

    private ConversionBO() {
        this.videoDAO = new VideoDAO();
        this.jobDAO = new ConversionJobDAO();
        this.ffmpegWrapper = new FFmpegWrapper();
        this.jobQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        this.executorService = Executors.newFixedThreadPool(WORKER_COUNT);
    }

    /**
     * Singleton pattern - Thread-safe
     */
    public static ConversionBO getInstance() {
        if (instance == null) {
            synchronized (ConversionBO.class) {
                if (instance == null) {
                    instance = new ConversionBO();
                }
            }
        }
        return instance;
    }

    /**
     * Khởi động workers và load pending jobs từ DB
     */
    public synchronized void startWorkers() {
        if (isRunning) return;

        isRunning = true;

        // Load pending jobs từ database
        List<ConversionJob> pendingJobs = jobDAO.getPendingJobs();
        int loaded = 0;
        for (ConversionJob job : pendingJobs) {
            if (jobQueue.offer(job)) loaded++;
        }
        System.out.println("[ConversionBO] Loaded " + loaded + " pending jobs");

        // Start workers
        for (int i = 0; i < WORKER_COUNT; i++) {
            executorService.submit(new ConversionWorker());
        }
        System.out.println("[ConversionBO] Started " + WORKER_COUNT + " workers");
    }

    /**
     * Dừng workers gracefully
     */
    public synchronized void stopWorkers() {
        isRunning = false;
        jobQueue.clear();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[ConversionBO] Stopped");
    }

    /**
     * Submit job mới vào queue
     */
    public ConversionJob submitJob(int userId, String videoFilename, String filePath,
                                    long fileSize, String outputFormat) {
        // 1. Lưu video vào DB
        Video video = new Video(userId, videoFilename, filePath, fileSize);
        if (!videoDAO.createVideo(video)) {
            return null;
        }

        // 2. Tạo job trong DB (status: PENDING)
        ConversionJob job = new ConversionJob(video.getVideoId(), userId, outputFormat);
        if (!jobDAO.createJob(job)) {
            return null;
        }

        // 3. Get job đầy đủ từ DB (có jobId)
        ConversionJob createdJob = jobDAO.getJobById(job.getJobId());
        if (createdJob == null) {
            return null;
        }

        // 4. Add vào queue
        boolean added = jobQueue.offer(createdJob);
        if (!added) {
            jobDAO.failJob(job.getJobId(), "Queue is full");
            return null;
        }

        return createdJob;
    }

    /**
     * Lấy tất cả jobs của user
     */
    public List<ConversionJob> getUserJobs(int userId) {
        return jobDAO.getJobsByUserId(userId);
    }

    /**
     * Xóa job và file output
     */
    public boolean deleteJob(int jobId, int userId) {
        ConversionJob job = jobDAO.getJobById(jobId);
        if (job == null || job.getUserId() != userId) {
            return false;
        }

        // Xóa file output nếu có
        if (job.getOutputPath() != null) {
            File file = new File(job.getOutputPath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("[ConversionBO] Cannot delete: " + file.getName());
                }
            }
        }

        return jobDAO.deleteJob(jobId);
    }

    /**
     * Tổng số conversions (dùng cho admin)
     */
    public int getTotalConversions() {
        return jobDAO.getConversionCountByUser().values().stream()
                .mapToInt(Integer::intValue).sum();
    }

    /**
     * Worker thread - Xử lý jobs từ queue
     */
    private class ConversionWorker implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    ConversionJob job = jobQueue.take(); // Block đến khi có job
                    processJob(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[Worker] Error: " + e.getMessage());
                }
            }
        }

        /**
         * Xử lý 1 job: Convert video
         */
        private void processJob(ConversionJob job) {
            try {
                // Update status: PROCESSING
                jobDAO.updateJobStatus(job.getJobId(), "PROCESSING", 0);

                // Lấy video từ DB
                Video video = videoDAO.getVideoById(job.getVideoId());
                if (video == null) {
                    jobDAO.failJob(job.getJobId(), "Video not found");
                    return;
                }

                // Check file tồn tại
                File inputFile = new File(video.getFilePath());
                if (!inputFile.exists()) {
                    jobDAO.failJob(job.getJobId(), "Video file not found");
                    return;
                }

                // Tạo output directory
                String outputFilename = getOutputFilename(video.getFilename(), job.getOutputFormat());
                String outputDir = inputFile.getParent() + File.separator + "converted";
                File outputDirFile = new File(outputDir);
                if (!outputDirFile.exists()) {
                    boolean created = outputDirFile.mkdirs();
                    if (!created) {
                        jobDAO.failJob(job.getJobId(), "Cannot create output directory");
                        return;
                    }
                }
                File outputFile = new File(outputDir, outputFilename);

                // Convert video bằng FFmpeg
                boolean success = ffmpegWrapper.convertVideo(
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    job.getOutputFormat(),
                    progress -> jobDAO.updateJobStatus(job.getJobId(), "PROCESSING", progress)
                );

                // Update kết quả
                if (success && outputFile.exists()) {
                    jobDAO.completeJob(job.getJobId(), outputFile.getAbsolutePath());
                } else {
                    jobDAO.failJob(job.getJobId(), "Conversion failed");
                }

            } catch (Exception e) {
                jobDAO.failJob(job.getJobId(), e.getMessage());
                System.err.println("[Worker] Job " + job.getJobId() + " failed: " + e.getMessage());
            }
        }

        /**
         * Tạo tên file output: video_converted.mp4
         */
        private String getOutputFilename(String originalFilename, String format) {
            int dotIndex = originalFilename.lastIndexOf('.');
            String baseName = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
            return baseName + "_converted." + format;
        }
    }
}

