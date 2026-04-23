// RetryUtils.java
package com.wtf.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);
    private static final long DEFAULT_RETRY_INTERVAL_MS = 10_000; // 10 seconds
    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final long DEFAULT_MAX_TOTAL_RETRY_TIME_MS = 300_000; // 5 minutes

    private RetryUtils() {
        // Utility class
    }

    /**
     * Executes a task with retry logic and exponential backoff.
     *
     * @param task The task to execute
     * @param maxRetries Maximum number of retry attempts
     * @param initialDelayMs Initial delay between retries in milliseconds
     * @param <T> Return type of the task
     * @return The result of the task
     * @throws Exception If all retry attempts fail, the last exception is thrown
     */
    public static <T> T withRetry(Supplier<T> task, int maxRetries, long initialDelayMs) throws Exception {
        int attempt = 0;
        Exception lastException;

        do {
            try {
                return task.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                if (attempt <= maxRetries) {
                    long waitTime = initialDelayMs * (long) Math.pow(2, attempt - 1);
                    logger.warn("Attempt {}/{} failed. Retrying in {} ms. Error: {} taskType: {}",
                            attempt, maxRetries, waitTime, e.getMessage(), ThreadLocalAutomationContext.getContext().getTaskType());
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry for TaskType: "+ThreadLocalAutomationContext.getContext().getTaskType(), ie);
                    }
                } else {
                    logger.error("All {} retry attempts failed for taskType:{}", maxRetries, ThreadLocalAutomationContext.getContext().getTaskType(),e);
                    throw lastException;
                }
            }
        } while (attempt <= maxRetries);

        // This line should never be reached
        throw new IllegalStateException("Unexpected error in retry logic for TaskType: "+ThreadLocalAutomationContext.getContext().getTaskType());
    }


    /**
     * Executes a task with retry logic and exponential backoff.
     * This is a convenience method for Runnable tasks that don't return a value.
     *
     * @param task The task to execute
     * @param maxRetries Maximum number of retry attempts
     * @param initialDelayMs Initial delay between retries in milliseconds
     * @throws Exception If all retry attempts fail, the last exception is thrown
     */
    public static void withRetry(Runnable task, int maxRetries, long initialDelayMs) throws Exception {
        withRetry(() -> {
            task.run();
            return null;
        }, maxRetries, initialDelayMs);
    }

    // Constants moved to the top of the class

    /**
     * Functional interface for operations that can be retried and return a result.
     * @param <T> The type of the result
     */
    @FunctionalInterface
    public interface RetryableSupplier<T> extends Supplier<T> {
        T getWithRetry() throws Exception;

        @Override
        default T get() {
            try {
                return getWithRetry();
            } catch (Exception e) {
                throw new RuntimeException("Operation failed after all retry attempts for TaskType: "+ThreadLocalAutomationContext.getContext().getTaskType(), e);
            }
        }
    }

    /**
     * Executes a callable with retry logic.
     *
     * @param callable The operation to execute
     * @param operationName Name of the operation for logging
     * @param maxRetries Maximum number of retry attempts
     * @param retryIntervalMs Interval between retries in milliseconds
     * @param <T> Return type of the operation
     * @return The result of the operation
     * @throws Exception If all retry attempts fail
     */
    public static <T> T withRetry(Callable<T> callable, String operationName,
                                  int maxRetries, long retryIntervalMs) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        final long startTime = System.currentTimeMillis();
        final long maxTotalTimeMs = Math.min(
            Long.getLong("maxRetryTimeMs", DEFAULT_MAX_TOTAL_RETRY_TIME_MS),
            DEFAULT_MAX_TOTAL_RETRY_TIME_MS
        );

        do {
            try {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > maxTotalTimeMs) {
                    String errorMsg = String.format("Operation '%s' timed out after %d ms (max %d ms)", 
                        operationName, elapsed, maxTotalTimeMs);
                    logger.error("{}. TaskType: {}", errorMsg, ThreadLocalAutomationContext.getContext().getTaskType());
                    throw new java.util.concurrent.TimeoutException(errorMsg);
                }

                logger.debug("Attempt {} of {} for operation: {} (elapsed: {}ms, max: {}ms) for TaskType: {}",
                    attempt + 1, maxRetries + 1, operationName, 
                    elapsed, maxTotalTimeMs,
                    ThreadLocalAutomationContext.getContext().getTaskType());
                    
                return callable.call();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry operation was interrupted for TaskType: "+ 
                    ThreadLocalAutomationContext.getContext().getTaskType(), ie);
            } catch (Exception e) {
                lastException = e;
                attempt++;

                if (attempt <= maxRetries) {
                    long waitTime = Math.min(
                        retryIntervalMs * (long) Math.pow(2, attempt - 1), // Exponential backoff
                        maxTotalTimeMs / 4 // But don't wait more than 1/4 of total time for a single retry
                    );
                    
                    logger.warn("Attempt {}/{} failed for operation: {} ({}). Retrying in {}ms... TaskType: {}",
                        attempt, maxRetries, operationName, e.getMessage(), 
                        waitTime, ThreadLocalAutomationContext.getContext().getTaskType());
                        
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry wait was interrupted for TaskType: "+ 
                            ThreadLocalAutomationContext.getContext().getTaskType(), ie);
                    }
                }
            }
        } while (attempt <= maxRetries);

        String errorMsg = String.format("Operation '%s' failed after %d attempts (total time: %dms)", 
            operationName, attempt, System.currentTimeMillis() - startTime);
            
        logger.error("{}. TaskType: {}", errorMsg, ThreadLocalAutomationContext.getContext().getTaskType(), lastException);
        throw lastException != null ? lastException : new RuntimeException(errorMsg);
    }

    /**
     * Executes a callable with default retry settings (3 retries, 30s interval).
     */
    public static <T> T withDefaultRetry(Callable<T> callable, String operationName) throws Exception {
        return withRetry(callable, operationName, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL_MS);
    }

    /**
     * Creates a retryable supplier with default settings.
     */
    public static <T> RetryableSupplier<T> retryable(RetryableSupplier<T> supplier) {
        return supplier;
    }

    /**
     * Creates a retryable supplier with custom settings.
     */
    public static <T> RetryableSupplier<T> retryable(RetryableSupplier<T> supplier,
                                                                                  int maxRetries, long retryIntervalMs) {
        return new RetryableSupplier<T>() {
            @Override
            public T getWithRetry() throws Exception {
                return withRetry(supplier::getWithRetry, "CustomRetryableOperation",
                        maxRetries, retryIntervalMs);
            }
        };
    }
}