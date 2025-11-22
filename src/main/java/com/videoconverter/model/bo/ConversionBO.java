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
 * ConversionBO - Business logic for video conversion operations
 * Manages conversion queue and worker threads
 */
public class ConversionBO {
    private static ConversionBO instance;

    private VideoDAO videoDAO;
    private ConversionJobDAO jobDAO;
    private FFmpegWrapper ffmpegWrapper;

    // Queue and workers
    private BlockingQueue<ConversionJob> jobQueue;
    private ExecutorService executorService;
    private static final int WORKER_COUNT = 2;
    private boolean isRunning = false;

    private ConversionBO() {
        this.videoDAO = new VideoDAO();
        this.jobDAO = new ConversionJobDAO();
        this.ffmpegWrapper = new FFmpegWrapper();
        this.jobQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newFixedThreadPool(WORKER_COUNT);
    }

    public static synchronized ConversionBO getInstance() {
        if (instance == null) {
            instance = new ConversionBO();
        }
        return instance;
    }

    /**
     * Start worker threads
     */
    public synchronized void startWorkers() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        // Load pending jobs from database
        List<ConversionJob> pendingJobs = jobDAO.getPendingJobs();
        jobQueue.addAll(pendingJobs);

        // Start worker threads
        for (int i = 0; i < WORKER_COUNT; i++) {
            executorService.submit(new ConversionWorker());
        }

        System.out.println("ConversionBO: Started " + WORKER_COUNT + " workers");
    }

    /**
     * Stop worker threads
     */
    public synchronized void stopWorkers() {
        isRunning = false;
        executorService.shutdownNow();
        System.out.println("ConversionBO: Stopped workers");
    }

    /**
     * Submit new conversion job
     */
    public ConversionJob submitJob(int userId, String videoFilename, String filePath, long fileSize, String outputFormat) {
        // Create video record
        Video video = new Video(userId, videoFilename, filePath, fileSize);
        if (!videoDAO.createVideo(video)) {
            return null;
        }

        // Create conversion job
        ConversionJob job = new ConversionJob(video.getVideoId(), userId, outputFormat);
        if (!jobDAO.createJob(job)) {
            return null;
        }

        // Add to queue
        jobQueue.offer(job);

        return job;
    }

    /**
     * Get user's conversion jobs
     */
    public List<ConversionJob> getUserJobs(int userId) {
        return jobDAO.getJobsByUserId(userId);
    }

    /**
     * Delete conversion job
     */
    public boolean deleteJob(int jobId, int userId) {
        ConversionJob job = jobDAO.getJobById(jobId);
        if (job == null || job.getUserId() != userId) {
            return false;
        }

        // Delete output file if exists
        if (job.getOutputPath() != null) {
            File file = new File(job.getOutputPath());
            if (file.exists()) {
                file.delete();
            }
        }

        return jobDAO.deleteJob(jobId);
    }

    /**
     * Get conversion statistics for admin
     */
    public int getTotalConversions() {
        return jobDAO.getConversionCountByUser().values().stream()
                .mapToInt(Integer::intValue).sum();
    }

    /**
     * Worker thread to process conversion jobs
     */
    private class ConversionWorker implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    ConversionJob job = jobQueue.take();
                    processJob(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private void processJob(ConversionJob job) {
            try {
                // Update status to PROCESSING
                jobDAO.updateJobStatus(job.getJobId(), "PROCESSING", 0);

                // Get video file
                Video video = videoDAO.getVideoById(job.getVideoId());
                if (video == null) {
                    jobDAO.failJob(job.getJobId(), "Video not found");
                    return;
                }

                File inputFile = new File(video.getFilePath());
                if (!inputFile.exists()) {
                    jobDAO.failJob(job.getJobId(), "Video file not found");
                    return;
                }

                // Prepare output file
                String outputFilename = getOutputFilename(video.getFilename(), job.getOutputFormat());
                String outputDir = inputFile.getParent() + File.separator + "converted";
                File outputDirFile = new File(outputDir);
                if (!outputDirFile.exists() && !outputDirFile.mkdirs()) {
                    jobDAO.failJob(job.getJobId(), "Failed to create output directory");
                    return;
                }
                File outputFile = new File(outputDir, outputFilename);

                // Convert video
                FFmpegWrapper wrapper = new FFmpegWrapper();
                boolean success = wrapper.convertVideo(
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    job.getOutputFormat(),
                    progress -> jobDAO.updateJobStatus(job.getJobId(), "PROCESSING", progress)
                );

                if (success && outputFile.exists()) {
                    jobDAO.completeJob(job.getJobId(), outputFile.getAbsolutePath());
                } else {
                    jobDAO.failJob(job.getJobId(), "Conversion failed");
                }

            } catch (Exception e) {
                jobDAO.failJob(job.getJobId(), e.getMessage());
                System.err.println("Conversion error: " + e.getMessage());
            }
        }

        private String getOutputFilename(String originalFilename, String format) {
            int dotIndex = originalFilename.lastIndexOf('.');
            String baseName = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
            return baseName + "_converted." + format;
        }
    }
}

