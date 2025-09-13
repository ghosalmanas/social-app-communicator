// RetryUtils.java
package com.wtf.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);

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

    private static final long DEFAULT_RETRY_INTERVAL_MS = 30_000; // 30 seconds
    private static final int DEFAULT_MAX_RETRIES = 3;

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
        Exception lastException;

        do {
            try {
                logger.debug("Attempt {} of {} for operation: {} for TaskType: {}",
                        attempt + 1, maxRetries + 1, operationName,ThreadLocalAutomationContext.getContext().getTaskType());
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                if (attempt <= maxRetries) {
                    logger.warn("Attempt {} failed for operation: {}. TaskType: {}. Retrying in {} ms...",
                            attempt, operationName, retryIntervalMs,ThreadLocalAutomationContext.getContext().getTaskType(), e);
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry operation was interrupted for TaskType: "+ThreadLocalAutomationContext.getContext().getTaskType(), ie);
                    }
                }
            }
        } while (attempt <= maxRetries);

        logger.error("Operation '{}' failed after {} attempts for TaskType: {}", operationName, attempt, ThreadLocalAutomationContext.getContext().getTaskType());
        throw lastException;
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