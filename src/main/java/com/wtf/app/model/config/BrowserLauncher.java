package com.wtf.app.model.config;

// Java imports
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

// Spring imports
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

// Log4j2 imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Configuration
@EnableScheduling
public class BrowserLauncher {

    private static final Logger logger = LogManager.getLogger(BrowserLauncher.class);
    private static final String DASHBOARD_URL = "http://localhost:8082/api-dashboard.html";

    private static final AtomicBoolean browserLaunched = new AtomicBoolean(false);
    private static final Object lock = new Object();
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Skip browser launch during tests
        if (isTestEnvironment(event)) {
            logger.info("Test environment detected, skipping browser launch");
            return;
        }

        // Only one thread will be able to execute this block at a time
        synchronized (lock) {
            // Double-check pattern to ensure thread safety
            if (browserLaunched.compareAndSet(false, true)) {
                launchBrowser();
            }
        }
    }

    private boolean isTestEnvironment(ApplicationReadyEvent event) {
        // Check for test profile
        String[] activeProfiles = event.getApplicationContext().getEnvironment().getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equalsIgnoreCase(profile)) {
                return true;
            }
        }

        // Check for Spring Boot test system property
        String isTest = System.getProperty("spring-boot.test");
        if (isTest != null && isTest.equalsIgnoreCase("true")) {
            return true;
        }

        // Check for JUnit test
        if (isRunningUnderJUnit()) {
            return true;
        }

        return false;
    }

    private boolean isRunningUnderJUnit() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private void launchBrowser() {
        logger.info("Attempting to launch browser...");
        
        logger.info("Java AWT Headless: {}", java.awt.GraphicsEnvironment.isHeadless());
        logger.info("OS: {}", System.getProperty("os.name"));
        
        try {
            // Try Windows start command first (most reliable on Windows)
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                logger.info("Using Windows start command to open browser");
                String url = DASHBOARD_URL;
                
                // Enclose URL in quotes in case it contains spaces or special characters
                String cmd = "cmd /c start " + "\"\" " + "\"" + url + "\"";
                logger.info("Executing command: {}", cmd);
                
                Process process = Runtime.getRuntime().exec(cmd);
                
                // Wait a bit to see if the command was successful
                Thread.sleep(1000);
                if (process.isAlive()) {
                    logger.info("Successfully launched browser using Windows start command");
                    return;
                }
                
                // Check if there was an error
                try {
                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        logger.warn("Browser launch command exited with code: {}", exitCode);
                    }
                } catch (IllegalThreadStateException e) {
                    // Process is still running, which is fine
                    logger.info("Browser process is still running");
                    return;
                }
            }
            
            // Fallback to Desktop API
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        logger.info("Falling back to Desktop API");
                        java.net.URI uri = new java.net.URI(DASHBOARD_URL);
                        desktop.browse(uri);
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("Desktop API failed: {}", e.getMessage());
                }
            }
            
            // Fallback to other platform-specific commands
            logger.info("Trying platform-specific fallback for OS: {}", os);
            
            if (os.contains("win")) {
                // Alternative Windows command
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", DASHBOARD_URL});
                logger.info("Launched browser using rundll32");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", DASHBOARD_URL});
                logger.info("Launched browser using open command");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", DASHBOARD_URL});
                logger.info("Launched browser using xdg-open");
            } else {
                logger.warn("Unsupported operating system: {}", os);
                logger.info("Please open the following URL in your browser: {}", DASHBOARD_URL);
            }
        } catch (Exception e) {
            logger.error("Failed to open browser: {}", e.getMessage(), e);
            logger.info("Please open the following URL in your browser: {}", DASHBOARD_URL);
        }
    }

    private URI getDashboardUri() throws URISyntaxException {
        return new URI(DASHBOARD_URL);
    }

    private boolean isHeadless() {
        // On Windows, we can safely assume it's not headless
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            logger.info("Windows detected, assuming non-headless environment");
            return false;
        }
        
        // Check if explicitly set to headless
        String headless = System.getProperty("java.awt.headless", System.getenv("JAVA_AWT_HEADLESS"));
        if (headless != null && headless.equalsIgnoreCase("true")) {
            logger.warn("Running in headless mode (java.awt.headless=true)");
            return true;
        }
        
        // Check common CI environment variables
        String ci = System.getenv("CI");
        if (ci != null && (ci.equalsIgnoreCase("true") || ci.equals("1"))) {
            logger.warn("Running in CI environment");
            return true;
        }
        
        // For Unix-like systems, check for display
        String[] headlessVars = {"DISPLAY", "WAYLAND_DISPLAY", "MIR_SOCKET"};
        for (String var : headlessVars) {
            String value = System.getenv(var);
            logger.debug("Checking environment variable {}: {}", var, value);
            if (value == null || value.trim().isEmpty()) {
                logger.warn("Running in headless environment ({} not set)", var);
                return true;
            }
        }
        
        logger.info("Running in non-headless environment");
        return false;
    }

    private void openWindowsBrowser() {
        try {
            URI dashboardUri = getDashboardUri();
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(dashboardUri);
            } else {
                // Fallback for environments without Desktop support
                String os = System.getProperty("os.name").toLowerCase();
                try {
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + DASHBOARD_URL);
                    } else if (os.contains("mac")) {
                        Runtime.getRuntime().exec("open " + DASHBOARD_URL);
                    } else if (os.contains("nix") || os.contains("nux")) {
                        Runtime.getRuntime().exec("xdg-open " + DASHBOARD_URL);
                    }
                } catch (IOException e) {
                    logger.error("Failed to open browser: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to open browser: {}", e.getMessage());
            try {
                // Last resort - try with default browser
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(getDashboardUri());
                }
            } catch (Exception ex) {
                logger.error("Failed to open with default browser: {}", ex.getMessage());
            }
        }
    }

    private void openMacBrowser() {
        try {
            Runtime.getRuntime().exec(new String[]{"open", DASHBOARD_URL});
        } catch (IOException e) {
            logger.error("Failed to open browser on Mac: {}", e.getMessage());
        }
    }

    private void openLinuxBrowser() {
        try {
            Runtime.getRuntime().exec(new String[]{"xdg-open", DASHBOARD_URL});
        } catch (IOException e) {
            logger.error("Failed to open browser on Linux: {}", e.getMessage());
        }
    }
}
