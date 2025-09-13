package com.wtf.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BuildAndRun {
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final String JAR_NAME = "WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar";
    private static final String TARGET_DIR = "target";
    private static final int PORT = 8082;
    private static final String PROFILE = "local";
    
    public void mains(String[] args) {
        try {
            printSection("Stopping any running Java processes...");
            killJavaProcesses();
            
            printSection("Cleaning and building the project...");
            if (runCommand("mvn", "clean", "install", "-DskipTests") != 0) {
                System.err.println("Build failed");
                System.exit(1);
            }
            
            printSection("Starting the application...");
            ProcessBuilder appProcess = new ProcessBuilder(
                "java",
                "-jar",
                Paths.get(TARGET_DIR, JAR_NAME).toString(),
                "--spring.profiles.active=" + PROFILE,
                "--server.port=" + PORT
            );
            appProcess.directory(new File(PROJECT_DIR));
            appProcess.inheritIO();
            
            // Start the application in a separate thread
            Thread appThread = new Thread(() -> {
                try {
                    Process process = appProcess.start();
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            appThread.start();
            
            // Wait for the application to start
            Thread.sleep(10000);
            
            printSection("Application started successfully on port " + PORT);
            
            // Call the API endpoint
            callApiEndpoint("http://localhost:" + PORT + "/api/automation/start");
            
            // Keep the application running
            appThread.join();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void killJavaProcesses() throws IOException, InterruptedException {
        if (isWindows()) {
            runCommand("taskkill", "/F", "/IM", "java.exe");
        } else {
            runCommand("pkill", "-f", "java");
        }
    }
    
    private static int runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(PROJECT_DIR));
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Print command output
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        
        return process.waitFor();
    }
    
    private static void callApiEndpoint(String urlString) {
        if (isWindows()) {
            try {
                // Use PowerShell's Invoke-WebRequest on Windows
                String command = String.format("powershell -Command \"Invoke-WebRequest -Uri '%s' -Method POST\"", urlString);
                System.out.println("Executing: " + command);
                
                Process process = Runtime.getRuntime().exec(command);
                
                // Read the output
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                
                // Read any errors
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println("Error: " + errorLine);
                    }
                }
                
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("PowerShell command failed with exit code: " + exitCode);
                }
            } catch (Exception e) {
                System.err.println("Error executing PowerShell command: " + e.getMessage());
            }
        } else {
            // Fallback to Java's HTTP client for non-Windows systems
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int status = connection.getResponseCode();
                System.out.println("API call to " + urlString + " returned status: " + status);
                
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    System.out.println("Response: " + content.toString());
                }
                
                connection.disconnect();
            } catch (Exception e) {
                System.err.println("Error calling API: " + e.getMessage());
            }
        }
    }
    
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
    
    private static void printSection(String message) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(message);
        System.out.println("=".repeat(60));
    }
}
