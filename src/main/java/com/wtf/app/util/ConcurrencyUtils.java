package com.wtf.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Utility class for concurrent operations and thread management.
 */
public class ConcurrencyUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtils.class);
    
    // Default executor service for utility methods
    private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Use daemon threads to allow JVM to exit
        t.setName("ConcurrencyUtils-Worker-" + t.getId());
        return t;
    });

    private ConcurrencyUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Execute a task with a timeout.
     *
     * @param task      The task to execute
     * @param timeout   The timeout duration
     * @param timeUnit  The time unit of the timeout
     * @param <T>       The return type of the task
     * @return The result of the task
     * @throws TimeoutException if the task times out
     * @throws ExecutionException if the task throws an exception
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T> T executeWithTimeout(Supplier<T> task, long timeout, TimeUnit timeUnit) 
            throws TimeoutException, ExecutionException, InterruptedException {
        Future<T> future = executorService.submit(task::get);
        try {
            return future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            future.cancel(true); // Attempt to cancel the task
            throw e;
        }
    }

    /**
     * Execute a task asynchronously and return a CompletableFuture.
     */
    public static <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executorService);
    }

    /**
     * Execute a task asynchronously with a timeout.
     */
    public static <T> CompletableFuture<T> executeAsyncWithTimeout(Supplier<T> task, long timeout, TimeUnit timeUnit) {
        return CompletableFuture.supplyAsync(task, executorService)
                .orTimeout(timeout, timeUnit);
    }

    /**
     * Shutdown the executor service.
     * This should be called when the application is shutting down.
     */
    public static void shutdown() {
        try {
            logger.info("Shutting down ConcurrencyUtils executor service");
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while shutting down executor service", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get the current thread pool status.
     */
    public static String getThreadPoolStatus() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return String.format(
                    "ThreadPool: Active=%d, Pool=%d, Core=%d, Max=%d, Queue=%d",
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getCorePoolSize(),
                    tpe.getMaximumPoolSize(),
                    tpe.getQueue().size()
            );
        }
        return "ThreadPool status not available";
    }
}
