package com.videoconverter.service;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.dao.UserDAO;
import com.videoconverter.dao.VideoDAO;
import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.ConversionJob.JobStatus;
import com.videoconverter.model.Video;
import com.videoconverter.util.FFmpegWrapper;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.*;

/**
 * ConversionService - Singleton service for managing video conversion jobs
 * Uses a thread pool and blocking queue to process conversion jobs asynchronously
 */
public class ConversionService {

    // Singleton instance
    private static volatile ConversionService instance;
    
    // Thread pool configuration
    private static final int WORKER_THREAD_COUNT = 3;
    
    // Executor service for worker threads
    private final ExecutorService executorService;
    
    // Blocking queue for conversion jobs
    private final BlockingQueue<ConversionJob> jobQueue;
    
    // DAOs
    private final ConversionJobDAO jobDAO;
    private final VideoDAO videoDAO;
    private final UserDAO userDAO;
    
    // Service state
    private volatile boolean isRunning;
    
    /**
     * Private constructor for Singleton pattern
     */
    private ConversionService() {
        this.jobQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);
        this.jobDAO = new ConversionJobDAO();
        this.videoDAO = new VideoDAO();
        this.userDAO = new UserDAO();
        this.isRunning = false;
        
        System.out.println("ConversionService initialized with " + WORKER_THREAD_COUNT + " worker threads");
    }
    
    /**
     * Get singleton instance (thread-safe double-checked locking)
     * 
     * @return ConversionService instance
     */
    public static ConversionService getInstance() {
        if (instance == null) {
            synchronized (ConversionService.class) {
                if (instance == null) {
                    instance = new ConversionService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Start the conversion service and worker threads
     */
    public synchronized void start() {
        if (isRunning) {
            System.out.println("ConversionService is already running");
            return;
        }
        
        isRunning = true;
        
        // Start worker threads
        for (int i = 0; i < WORKER_THREAD_COUNT; i++) {
            final int workerId = i + 1;
            executorService.submit(new ConversionWorker(workerId));
        }
        
        System.out.println("ConversionService started successfully with " + 
                         WORKER_THREAD_COUNT + " workers");
    }
    
    /**
     * Stop the conversion service gracefully
     */
    public synchronized void stop() {
        if (!isRunning) {
            System.out.println("ConversionService is not running");
            return;
        }
        
        isRunning = false;
        
        System.out.println("Stopping ConversionService...");
        
        // Shutdown executor service
        executorService.shutdown();
        
        try {
            // Wait for all tasks to complete
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // Force shutdown if tasks don't complete
                executorService.shutdownNow();
                
                // Wait again
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate");
                }
            }
            System.out.println("ConversionService stopped successfully");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("ConversionService shutdown interrupted: " + e.getMessage());
        }
    }
    
    /**
     * Submit a conversion job to the queue
     * 
     * @param job Conversion job to submit
     * @return true if job was added to queue successfully, false otherwise
     */
    public boolean submitJob(ConversionJob job) {
        if (!isRunning) {
            System.err.println("ConversionService is not running. Cannot submit job.");
            return false;
        }
        
        if (job == null) {
            System.err.println("Cannot submit null job");
            return false;
        }
        
        try {
            // Set initial status if not set
            if (job.getStatus() == null) {
                job.setStatus(JobStatus.PENDING);
            }
            
            // Add to queue
            boolean added = jobQueue.offer(job, 5, TimeUnit.SECONDS);
            
            if (added) {
                System.out.println("Job submitted successfully: Job ID " + job.getJobId() + 
                                 " (Queue size: " + jobQueue.size() + ")");
                return true;
            } else {
                System.err.println("Failed to submit job: Queue is full");
                return false;
            }
        } catch (InterruptedException e) {
            System.err.println("Job submission interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Get current queue size
     * 
     * @return Number of jobs in queue
     */
    public int getQueueSize() {
        return jobQueue.size();
    }
    
    /**
     * Check if service is running
     * 
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Load pending jobs from database and submit to queue
     * (Call this on service startup to resume pending jobs)
     * 
     * @return Number of jobs loaded
     */
    public int loadPendingJobs() {
        try {
            var pendingJobs = jobDAO.findPendingJobs(100);
            int count = 0;
            
            for (ConversionJob job : pendingJobs) {
                if (submitJob(job)) {
                    count++;
                }
            }
            
            System.out.println("Loaded " + count + " pending jobs from database");
            return count;
        } catch (Exception e) {
            System.err.println("Error loading pending jobs: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * ConversionWorker - Worker thread that processes conversion jobs
     */
    private class ConversionWorker implements Runnable {
        private final int workerId;
        
        public ConversionWorker(int workerId) {
            this.workerId = workerId;
        }
        
        @Override
        public void run() {
            System.out.println("Worker #" + workerId + " started");
            
            while (isRunning) {
                try {
                    // Take job from queue (blocks if queue is empty)
                    ConversionJob job = jobQueue.poll(1, TimeUnit.SECONDS);
                    
                    if (job != null) {
                        processJob(job);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Worker #" + workerId + " interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Worker #" + workerId + " error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Worker #" + workerId + " stopped");
        }
        
        /**
         * Process a conversion job
         * 
         * @param job Job to process
         */
        private void processJob(ConversionJob job) {
            System.out.println("Worker #" + workerId + " processing Job ID: " + job.getJobId());
            
            try {
                // Update job status to PROCESSING
                job.setStatus(JobStatus.PROCESSING);
                job.setStartedAt(new Timestamp(System.currentTimeMillis()));
                jobDAO.update(job);
                
                // Get video information
                Video video = videoDAO.findById(job.getVideoId());
                if (video == null) {
                    throw new Exception("Video not found: ID " + job.getVideoId());
                }
                
                // Prepare input and output paths
                String inputPath = video.getFilePath();
                
                File inputFile = new File(inputPath);
                if (!inputFile.exists()) {
                    throw new Exception("Input file does not exist: " + inputPath);
                }
                
                // Generate output filename and path
                String outputDir = getOutputDirectory(inputPath);
                String outputFilename = generateOutputFilename(video.getOriginalFilename(), 
                                                               job.getOutputFormat());
                String outputPath = outputDir + File.separator + outputFilename;
                
                // Ensure output directory exists
                new File(outputDir).mkdirs();
                
                System.out.println("Converting: " + inputPath + " -> " + outputPath);
                
                // Convert video using FFmpegWrapper
                boolean success = FFmpegWrapper.convertVideo(
                    inputPath,
                    outputPath,
                    job.getConversionSettings(),
                    new FFmpegWrapper.ProgressListener() {
                        @Override
                        public void onProgress(int progress, int timeProcessed, String message) {
                            // Update progress in database
                            job.setProgress(progress);
                            jobDAO.updateProgress(job.getJobId(), progress);
                            
                            System.out.println("Job #" + job.getJobId() + 
                                             " progress: " + progress + "%");
                        }
                        
                        @Override
                        public void onComplete() {
                            System.out.println("Job #" + job.getJobId() + " conversion completed");
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            System.err.println("Job #" + job.getJobId() + 
                                             " conversion error: " + errorMessage);
                        }
                    }
                );
                
                if (success) {
                    // Get output file size
                    File outputFile = new File(outputPath);
                    long outputSize = outputFile.length();
                    
                    // Mark job as completed
                    job.setStatus(JobStatus.COMPLETED);
                    job.setProgress(100);
                    job.setCompletedAt(new Timestamp(System.currentTimeMillis()));
                    job.setOutputFilename(outputFilename);
                    job.setOutputPath(outputPath);
                    job.setOutputSize(outputSize);
                    jobDAO.update(job);
                    
                    // Increment user's total conversions
                    userDAO.incrementTotalConversions(job.getUserId());
                    
                    System.out.println("Job #" + job.getJobId() + 
                                     " completed successfully (Size: " + 
                                     job.getOutputSizeFormatted() + ")");
                } else {
                    throw new Exception("FFmpeg conversion failed");
                }
                
            } catch (Exception e) {
                // Handle error
                String errorMessage = "Conversion failed: " + e.getMessage();
                System.err.println("Job #" + job.getJobId() + " - " + errorMessage);
                e.printStackTrace();
                
                // Check if can retry
                if (job.canRetry()) {
                    System.out.println("Job #" + job.getJobId() + 
                                     " will be retried (attempt " + 
                                     (job.getRetryCount() + 1) + "/" + 
                                     job.getMaxRetries() + ")");
                    
                    // Reset status to PENDING for retry
                    job.setStatus(JobStatus.PENDING);
                    job.incrementRetryCount();
                    jobDAO.update(job);
                    
                    // Re-submit to queue
                    submitJob(job);
                } else {
                    // Mark as failed
                    job.setStatus(JobStatus.FAILED);
                    job.setErrorMessage(errorMessage);
                    job.setCompletedAt(new Timestamp(System.currentTimeMillis()));
                    jobDAO.update(job);
                    
                    System.err.println("Job #" + job.getJobId() + 
                                     " failed permanently after " + 
                                     job.getRetryCount() + " retries");
                }
            }
        }
        
        /**
         * Get output directory based on input path
         * 
         * @param inputPath Input file path
         * @return Output directory path
         */
        private String getOutputDirectory(String inputPath) {
            File inputFile = new File(inputPath);
            File parentDir = inputFile.getParentFile();
            
            // Get the uploads directory
            File uploadsDir = parentDir;
            
            // Get the webapp directory (parent of uploads)
            File webappDir = uploadsDir.getParentFile();
            
            // Create/get converted directory
            File convertedDir = new File(webappDir, "converted");
            
            return convertedDir.getAbsolutePath();
        }
        
        /**
         * Generate output filename
         * 
         * @param originalFilename Original filename
         * @param outputFormat Output format
         * @return Generated output filename
         */
        private String generateOutputFilename(String originalFilename, String outputFormat) {
            // Remove extension from original filename
            int lastDot = originalFilename.lastIndexOf('.');
            String nameWithoutExt = lastDot > 0 ? 
                                   originalFilename.substring(0, lastDot) : 
                                   originalFilename;
            
            // Add timestamp to avoid conflicts
            long timestamp = System.currentTimeMillis();
            
            // Generate new filename
            return nameWithoutExt + "_converted_" + timestamp + "." + outputFormat;
        }
    }
    
    /**
     * Get service statistics
     * 
     * @return Statistics string
     */
    public String getStatistics() {
        return "ConversionService Statistics:\n" +
               "  Running: " + isRunning + "\n" +
               "  Worker Threads: " + WORKER_THREAD_COUNT + "\n" +
               "  Queue Size: " + jobQueue.size() + "\n" +
               "  Active Threads: " + 
               (executorService instanceof ThreadPoolExecutor ? 
                ((ThreadPoolExecutor) executorService).getActiveCount() : "N/A");
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("Testing ConversionService...");
        System.out.println("========================================");
        
        // Get instance
        ConversionService service = ConversionService.getInstance();
        
        // Check if FFmpeg is available
        if (!FFmpegWrapper.isFFmpegAvailable()) {
            System.err.println("FFmpeg is not available. Please install FFmpeg first.");
            return;
        }
        
        // Start service
        service.start();
        
        // Print statistics
        System.out.println(service.getStatistics());
        
        // Load pending jobs from database
        int loaded = service.loadPendingJobs();
        System.out.println("Loaded " + loaded + " pending jobs");
        
        // Keep service running
        System.out.println("\nConversionService is running...");
        System.out.println("Press Ctrl+C to stop");
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            service.stop();
        }));
        
        // Wait indefinitely
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted");
        }
    }
}
