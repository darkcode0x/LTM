package com.videoconverter.listener;

import com.videoconverter.service.ConversionService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application lifecycle listener for Video Converter application.
 * Handles startup and shutdown tasks including:
 * - Starting ConversionService worker threads
 * - Loading pending conversion jobs
 * - Gracefully shutting down services
 */
@WebListener
public class AppContextListener implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());
    
    /**
     * Called when the application starts.
     * Initializes and starts the ConversionService.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("========================================");
        LOGGER.info("Video Converter Application Starting...");
        LOGGER.info("========================================");
        
        try {
            // Get ConversionService instance
            ConversionService conversionService = ConversionService.getInstance();
            
            // Start the service (worker threads)
            conversionService.start();
            
            LOGGER.info("ConversionService started successfully");
            LOGGER.info("Worker threads: " + getWorkerThreadCount());
            
            // Load pending jobs from database
            int pendingJobs = conversionService.loadPendingJobs();
            LOGGER.info("Loaded " + pendingJobs + " pending conversion jobs");
            
            // Store service in servlet context for future access if needed
            sce.getServletContext().setAttribute("conversionService", conversionService);
            
            LOGGER.info("========================================");
            LOGGER.info("Video Converter Application Started Successfully!");
            LOGGER.info("========================================");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Video Converter Application", e);
            throw new RuntimeException("Application startup failed", e);
        }
    }
    
    /**
     * Called when the application shuts down.
     * Gracefully stops the ConversionService.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("========================================");
        LOGGER.info("Video Converter Application Shutting Down...");
        LOGGER.info("========================================");
        
        try {
            // Get ConversionService from context
            ConversionService conversionService = (ConversionService) 
                sce.getServletContext().getAttribute("conversionService");
            
            if (conversionService != null) {
                LOGGER.info("Stopping ConversionService...");
                conversionService.stop();
                LOGGER.info("ConversionService stopped successfully");
            }
            
            // Remove from context
            sce.getServletContext().removeAttribute("conversionService");
            
            LOGGER.info("========================================");
            LOGGER.info("Video Converter Application Shut Down Successfully!");
            LOGGER.info("========================================");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during application shutdown", e);
        }
    }
    
    /**
     * Get the number of worker threads from system property or default to 3
     */
    private int getWorkerThreadCount() {
        String threadCount = System.getProperty("conversion.worker.threads", "3");
        try {
            return Integer.parseInt(threadCount);
        } catch (NumberFormatException e) {
            return 3;
        }
    }
}
