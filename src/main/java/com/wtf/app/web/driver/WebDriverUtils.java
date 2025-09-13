package com.wtf.app.web.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for WebDriver-related operations.
 */
public class WebDriverUtils {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverUtils.class);

    /**
     * Creates ChromeOptions with standard configuration.
     *
     * @param headless Whether to run in headless mode
     * @return Configured ChromeOptions
     */
    public static ChromeOptions createChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        
        // Basic Chrome options
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-software-rasterizer",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--remote-debugging-port=0",
            "--disable-browser-side-navigation",
            "--disable-features=IsolateOrigins,site-per-process"
        );

        // Generate a unique user data directory for each instance
        String userDataDir = System.getProperty("java.io.tmpdir") + "/chrome-profiles/" + UUID.randomUUID();
        new File(userDataDir).mkdirs();
        options.addArguments("--user-data-dir=" + userDataDir);

        // Configure headless mode if needed
        if (headless) {
            options.addArguments("--headless=new");
        }

        // Set up mobile emulation - only if not in headless mode
        /*if (!headless) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPhone X");
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }
        */
        // Disable automation flags
        options.setExperimentalOption("excludeSwitches", 
            Arrays.asList("enable-automation", "load-extension"));
        options.setExperimentalOption("useAutomationExtension", false);
        
        // Set window size
        options.addArguments("--window-size=1920,1080");

        // Enable browser logging
        options.setCapability("goog:loggingPrefs", Map.of("browser", "ALL"));

        // Set accept insecure certs
        options.setAcceptInsecureCerts(true);

        return options;
    }

    /**
     * Safely quits a WebDriver instance.
     *
     * @param driver The WebDriver to quit (can be null)
     */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                logger.debug("Successfully quit WebDriver instance");
            } catch (Exception e) {
                logger.warn("Error while quitting WebDriver: {}", e.getMessage());
            }
        }
    }

    /**
     * Cleans up any running Chrome/Chromedriver processes.
     */
    public static void cleanupChromeProcesses() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("taskkill /F /IM chrome.exe /T");
                Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
            } else {
                // Unix/Linux/Mac
                Runtime.getRuntime().exec("pkill -f chrome");
                Runtime.getRuntime().exec("pkill -f chromedriver");
            }

            // Give OS time to clean up
            Thread.sleep(2000);
        } catch (Exception e) {
            logger.warn("Error cleaning up Chrome processes: {}", e.getMessage());
        }
    }

    /**
     * Creates a new WebDriver instance with retry logic.
     *
     * @param options ChromeOptions to use
     * @param maxRetries Maximum number of retry attempts
     * @return A new WebDriver instance
     * @throws RuntimeException if all retry attempts fail
     */
    public static WebDriver createWebDriverWithRetry(ChromeOptions options, int maxRetries) {
        int attempt = 0;
        Exception lastError = null;

        while (attempt < maxRetries) {
            try {
                return new ChromeDriver(options);
            } catch (Exception e) {
                lastError = e;
                attempt++;
                logger.warn("WebDriver creation attempt {}/{}", attempt, maxRetries, e);

                // Clean up any lingering Chrome processes
                cleanupChromeProcesses();

                // Add delay between retries
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during WebDriver creation", ie);
                }
            }
        }

        throw new RuntimeException("Failed to create WebDriver after " + maxRetries + " attempts", lastError);
    }
}
