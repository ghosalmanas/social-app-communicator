package com.wtf.app.config;

import com.wtf.app.util.SessionKeepAlive;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for session management components.
 */
@Configuration
@EnableScheduling
public class SessionConfig {
    
    /**
     * Creates and configures the SessionKeepAlive bean.
     * @return A new instance of SessionKeepAlive
     */
    @Bean
    public SessionKeepAlive sessionKeepAlive() {
        return new SessionKeepAlive();
    }
}
