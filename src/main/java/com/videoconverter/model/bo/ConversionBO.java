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

public class ConversionBO {
    private static volatile ConversionBO instance;

    private final VideoDAO videoDAO;
    private final ConversionJobDAO jobDAO;
    private final FFmpegWrapper ffmpegWrapper;
    private final BlockingQueue<ConversionJob> jobQueue;
    private final ExecutorService executorService;

    private static final int WORKER_COUNT = 3;
    private static final int MAX_QUEUE_SIZE = 50;
    private volatile boolean isRunning = false;

    private ConversionBO() {
        this.videoDAO = new VideoDAO();
        this.jobDAO = new ConversionJobDAO();
        this.ffmpegWrapper = new FFmpegWrapper();
        this.jobQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        this.executorService = Executors.newFixedThreadPool(WORKER_COUNT);
    }

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

    public synchronized void startWorkers() {
        if (isRunning) return;

        isRunning = true;

        List<ConversionJob> pendingJobs = jobDAO.getPendingJobs();
        for (ConversionJob job : pendingJobs) {
            jobQueue.offer(job);
        }

        for (int i = 0; i < WORKER_COUNT; i++) {
            executorService.submit(new ConversionWorker());
        }
    }

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
    }

    public ConversionJob submitJob(int userId, String videoFilename, String filePath,
                                    long fileSize, String outputFormat) {
        Video video = new Video(userId, videoFilename, filePath, fileSize);
        if (!videoDAO.createVideo(video)) {
            return null;
        }

        ConversionJob job = new ConversionJob(video.getVideoId(), userId, outputFormat);
        if (!jobDAO.createJob(job)) {
            return null;
        }

        ConversionJob createdJob = jobDAO.getJobById(job.getJobId());
        if (createdJob == null) {
            return null;
        }

        boolean added = jobQueue.offer(createdJob);
        if (!added) {
            jobDAO.failJob(job.getJobId(), "Queue is full");
            return null;
        }

        return createdJob;
    }

    public List<ConversionJob> getUserJobs(int userId) {
        return jobDAO.getJobsByUserId(userId);
    }

    public boolean deleteJob(int jobId, int userId) {
        ConversionJob job = jobDAO.getJobById(jobId);
        if (job == null || job.getUserId() != userId) {
            return false;
        }

        if (job.getOutputPath() != null) {
            File file = new File(job.getOutputPath());
            if (file.exists()) {
                file.delete();
            }
        }

        return jobDAO.deleteJob(jobId);
    }


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
                } catch (Exception e) {
                    System.err.println("[Worker] Error: " + e.getMessage());
                }
            }
        }

        private void processJob(ConversionJob job) {
            try {
                jobDAO.updateJobStatus(job.getJobId(), "PROCESSING", 0);

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

                boolean success = ffmpegWrapper.convertVideo(
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
            }
        }

        private String getOutputFilename(String originalFilename, String format) {
            int dotIndex = originalFilename.lastIndexOf('.');
            String baseName = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
            return baseName + "_converted." + format;
        }
    }
}

