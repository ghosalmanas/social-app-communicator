package com.wtf.app.service.parallel;

import org.openqa.selenium.WebDriver;

/**
 * Functional interface for tasks that need to be executed with a WebDriver instance.
 */
@FunctionalInterface
public interface WebDriverTask {
    /**
     * Execute a task with the provided WebDriver instance.
     *
     * @param driver The WebDriver instance to use for the task
     * @return true if the task completed successfully, false otherwise
     * @throws Exception if an error occurs during task execution
     */
    boolean execute(WebDriver driver) throws Exception;
}
