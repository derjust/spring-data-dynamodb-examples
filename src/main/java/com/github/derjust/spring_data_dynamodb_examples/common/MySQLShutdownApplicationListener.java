package com.github.derjust.spring_data_dynamodb_examples.common;

import com.github.derjust.spring_data_dynamodb_examples.rest.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class MySQLShutdownApplicationListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        log.info("AbandonedConnectionCleanupThread - Shutdown initiated...");
        com.mysql.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        log.info("AbandonedConnectionCleanupThread - Completed");
    }
}
