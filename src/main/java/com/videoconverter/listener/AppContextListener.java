package com.videoconverter.listener;

import com.videoconverter.model.bo.ConversionBO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[App] Starting...");
        ConversionBO.getInstance().startWorkers();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[App] Stopping...");
        ConversionBO.getInstance().stopWorkers();
    }
}


