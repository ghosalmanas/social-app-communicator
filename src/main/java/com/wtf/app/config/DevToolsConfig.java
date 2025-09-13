package com.wtf.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevToolsConfig {

    /**
     * Configure DevTools to ignore Selenium-related classes and resources
     * to prevent unnecessary restarts during development.
     */
    @Bean
    public String configureDevTools() { // Enable DevTools features
        System.setProperty("spring.devtools.restart.enabled", "true");
        System.setProperty("spring.devtools.livereload.enabled", "true");

        // Set reasonable timeouts
        System.setProperty("spring.devtools.restart.poll-interval", "1s");
        System.setProperty("spring.devtools.restart.quiet-period", "400ms");

        // Exclude only build and IDE files
        System.setProperty("spring.devtools.restart.exclude",
                "static/**,templates/**,public/**,resources/**,META-INF/**,**/*.js,**/*.css,**/*.html");

        return "DevTools configuration applied successfully";
    }
}