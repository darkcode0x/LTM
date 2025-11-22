package com.videoconverter.listener;

import com.videoconverter.model.bo.ConversionBO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Video Converter Application Starting ===");

        // Start conversion workers
        ConversionBO conversionBO = ConversionBO.getInstance();
        conversionBO.startWorkers();

        System.out.println("=== Application Started Successfully ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Video Converter Application Stopping ===");

        // Stop conversion workers
        ConversionBO conversionBO = ConversionBO.getInstance();
        conversionBO.stopWorkers();

        System.out.println("=== Application Stopped ===");
    }
}


