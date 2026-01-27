package com.zhukovskiy.web.controller.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ServletContextListenerImpl implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("context initialized {}", sce.getServletContext().getServletContextName());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("context destroyed {}", sce.getServletContext().getServletContextName());
    }
}
