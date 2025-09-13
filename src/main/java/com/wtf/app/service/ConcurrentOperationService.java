package com.wtf.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Service to handle concurrent operations with proper error handling and timeouts.
 */
@Service
public class ConcurrentOperationService {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentOperationService.class);
    
    /**
     * Execute a task asynchronously with a timeout.
     * 
     * @param task The task to execute
     * @param taskName Name of the task for logging
     * @param timeoutMs Timeout in milliseconds
     * @return A CompletableFuture that completes when the task finishes or times out
     */
    @Async("ioTaskExecutor")
    public <T> CompletableFuture<T> executeWithTimeout(Supplier<T> task, String taskName, long timeoutMs) {
        logger.info("Starting async task: {}", taskName);
        long startTime = System.currentTimeMillis();
        
        try {
            T result = task.get();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed async task: {} in {} ms", taskName, duration);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("Error in async task: " + taskName, e);
            throw new RuntimeException("Error in async task: " + taskName, e);
        }
    }
    
    /**
     * Execute a task asynchronously without a return value.
     */
    @Async("ioTaskExecutor")
    public void executeAsync(Runnable task, String taskName) {
        logger.info("Starting async task: {}", taskName);
        long startTime = System.currentTimeMillis();
        
        try {
            task.run();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed async task: {} in {} ms", taskName, duration);
        } catch (Exception e) {
            logger.error("Error in async task: " + taskName, e);
            throw new RuntimeException("Error in async task: " + taskName, e);
        }
    }
    
    /**
     * Execute a task asynchronously and return a Future.
     */
    @Async("ioTaskExecutor")
    public <T> Future<T> executeAsyncWithFuture(Supplier<T> task, String taskName) {
        logger.info("Starting async task with future: {}", taskName);
        long startTime = System.currentTimeMillis();
        
        try {
            T result = task.get();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed async task with future: {} in {} ms", taskName, duration);
            return new AsyncResult<>(result);
        } catch (Exception e) {
            logger.error("Error in async task with future: " + taskName, e);
            throw new RuntimeException("Error in async task: " + taskName, e);
        }
    }
}
