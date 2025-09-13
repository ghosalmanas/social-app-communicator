package com.wtf.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable Spring's scheduled task execution and async support.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {
    // Configuration for enabling scheduled tasks and async execution
}
