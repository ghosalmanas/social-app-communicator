package com.wtf.app.config;

import com.wtf.app.web.driver.ParallelWebDriverManager;
import com.wtf.app.web.driver.WebDriverHealthMonitor;
import com.wtf.app.web.driver.WebDriverHealthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for WebDriver health monitoring.
 * Enables scheduling and async processing for health checks.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class WebDriverHealthConfig {
    
    /**
     * Creates and configures the WebDriverHealthService bean.
     * @param driverManager The ParallelWebDriverManager to monitor
     * @return A configured WebDriverHealthService instance
     */
    @Bean
    public WebDriverHealthService webDriverHealthService(ParallelWebDriverManager driverManager, ApplicationEventPublisher eventPublisher) {
        return new WebDriverHealthMonitor(driverManager, eventPublisher);
    }
    
    /**
     * Creates and configures the WebDriverHealthMonitor bean.
     * @param driverManager The ParallelWebDriverManager to monitor
     * @return A configured WebDriverHealthMonitor instance
     */
    @Bean
    public WebDriverHealthMonitor webDriverHealthMonitor(ParallelWebDriverManager driverManager, ApplicationEventPublisher eventPublisher) {
        return new WebDriverHealthMonitor(driverManager, eventPublisher);
    }
}
