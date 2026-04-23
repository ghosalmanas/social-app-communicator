package com.wtf.app.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeTest {
    public static void main(String[] args) {
        try {
            System.out.println("Setting up ChromeDriver...");

            // This will automatically download and setup ChromeDriver
            WebDriverManager.chromedriver().timeout(50).setup();

            // Optional: Configure Chrome options
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            System.out.println("Launching Chrome...");
            WebDriver driver = new ChromeDriver(options);

            // Test navigation
            System.out.println("Navigating to Google...");
            driver.get("https://www.google.com");

            // Print page title
            System.out.println("Page title: " + driver.getTitle());

            // Clean up
            System.out.println("Closing Chrome...");
            driver.quit();
            System.out.println("Test completed successfully!");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}