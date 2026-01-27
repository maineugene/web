package com.zhukovskiy.web.controller.listener;

import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionAttributeListenerImpl implements HttpSessionAttributeListener {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        logger.info("session attribute added {}", event.getSession().getAttribute("user_name"));
        logger.info("session attribute added {}", event.getSession().getAttribute("current page"));
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        logger.info("session attribute replaced {}", event.getSession().getAttribute("user_name"));
        logger.info("session attribute replaced {}", event.getSession().getAttribute("current page"));
    }
}
