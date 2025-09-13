package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.web.driver.ParallelWebDriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;

@Component("telegramParent")
public abstract class TelegramParent extends SocialParentCommonUtils {
    private static final Logger logger = LogManager.getLogger(TelegramParent.class);

    // Request-scoped collections stored in RequestContext
    protected Set<String> alreadyTraversedGroupsOnly;
    protected Set<String> alreadyTraversedGroupsAndIndividuals;

    @Autowired
    public TelegramParent(
                          InitialSetup initialSetup,
                          ParallelWebDriverManager parallelWebDriverManager) {
        super( initialSetup, parallelWebDriverManager);
        logger.info("TelegramParent initialized with InitialSetup and ParallelWebDriverManager");
    }

    // Add common Telegram-specific methods here
    // These methods will be implemented by concrete implementations
    
    /**
     * Navigate to Telegram Web and wait for login
     */
    public abstract void navigateAndWaitForLogin();
    
    /**
     * Send message to a specific chat/group
     */
    public abstract void sendMessage(String chatName, String message);
    
    /**
     * Fetch all groups and their information
     */
    public abstract List<Map<String, String>> fetchAllGroups();
    
    /**
     * Check if user is logged in to Telegram Web
     */
    public abstract boolean isLoggedIn();
    
    // Common utility methods for Telegram can be added here
    
    @Override
    public String toString() {
        return "TelegramParent{" +
                "alreadyTraversedGroupsOnly=" + alreadyTraversedGroupsOnly.size() +
                ", alreadyTraversedGroupsAndIndividuals=" + alreadyTraversedGroupsAndIndividuals.size() +
                '}';
    }
}
