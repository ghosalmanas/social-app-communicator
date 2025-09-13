package com.wtf.app.web.driver;

import com.wtf.app.model.enums.TaskType;

/**
 * Defines the contract for WebDriver health monitoring services.
 */
public interface WebDriverHealthService {
    
    /**
     * Registers a WebDriver session for health monitoring.
     * @param taskType The profile name associated with the WebDriver
     */
    void registerSession(TaskType taskType);
    
    /**
     * Unregisters a WebDriver session from health monitoring.
     * @param taskType The profile name to unregister
     */
    void unregisterSession(TaskType taskType);
    
    /**
     * Updates the last activity time for a session.
     * @param taskType The profile name to update
     */
    void updateActivityTime(TaskType taskType);
    
    /**
     * Performs an immediate health check on a session.
     * @param taskType The profile name to check
     * @return true if the session is healthy, false otherwise
     */
    boolean checkSessionHealth(TaskType taskType);
    
    /**
     * Attempts to recover a failed session.
     * @param taskType The profile name to recover
     * @return true if recovery was successful, false otherwise
     */
    boolean recoverSession(TaskType taskType);
    
    /**
     * Refreshes a session to prevent timeouts.
     * @param taskType The profile name to refresh
     */
    void refreshSession(TaskType taskType);
}
