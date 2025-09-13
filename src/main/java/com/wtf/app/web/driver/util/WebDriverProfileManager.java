package com.wtf.app.web.driver.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Manages WebDriver profiles including creation, cleanup, and directory management.
 */
@Component
public class WebDriverProfileManager {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverProfileManager.class);
    
    @Value("${webdriver.profile.base-dir:./profiles}")
    private String baseProfileDir;
    
    @Value("${webdriver.profile.prefix:profile_}")
    private String profilePrefix;
    
    /**
     * Creates a new profile directory with a unique name.
     *
     * @return the path to the created profile directory
     * @throws IOException if an I/O error occurs
     */
    public String createProfile() throws IOException {
        ensureBaseDirectoryExists();
        String profileId = profilePrefix + UUID.randomUUID().toString();
        Path profilePath = Paths.get(baseProfileDir, profileId);
        
        if (!Files.exists(profilePath)) {
            Files.createDirectories(profilePath);
            logger.debug("Created new profile directory: {}", profilePath);
        }
        
        return profilePath.toString();
    }
    
    /**
     * Deletes a profile directory and all its contents.
     *
     * @param profilePath the path to the profile directory to delete
     * @return true if the directory was successfully deleted, false otherwise
     */
    public boolean deleteProfile(String profilePath) {
        if (profilePath == null || profilePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            File profileDir = new File(profilePath);
            if (profileDir.exists() && profileDir.isDirectory()) {
                FileUtils.deleteDirectory(profileDir);
                logger.debug("Deleted profile directory: {}", profilePath);
                return true;
            }
        } catch (IOException e) {
            logger.error("Failed to delete profile directory: {}", profilePath, e);
        }
        
        return false;
    }
    
    /**
     * Ensures that the base profile directory exists.
     *
     * @throws IOException if the directory cannot be created
     */
    private void ensureBaseDirectoryExists() throws IOException {
        Path basePath = Paths.get(baseProfileDir);
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
            logger.debug("Created base profile directory: {}", basePath);
        }
    }
    
    /**
     * Cleans up all profile directories.
     *
     * @return the number of profiles deleted
     */
    public int cleanupAllProfiles() {
        File baseDir = new File(baseProfileDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return 0;
        }
        
        int deletedCount = 0;
        File[] profileDirs = baseDir.listFiles(File::isDirectory);
        
        if (profileDirs != null) {
            for (File profileDir : profileDirs) {
                if (profileDir.getName().startsWith(profilePrefix)) {
                    if (deleteProfile(profileDir.getAbsolutePath())) {
                        deletedCount++;
                    }
                }
            }
        }
        
        logger.info("Cleaned up {} profile directories", deletedCount);
        return deletedCount;
    }
    
    /**
     * Gets the number of active profiles.
     *
     * @return the number of active profile directories
     */
    public int getActiveProfileCount() {
        File baseDir = new File(baseProfileDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return 0;
        }
        
        File[] profileDirs = baseDir.listFiles(File::isDirectory);
        return profileDirs != null ? profileDirs.length : 0;
    }
}
