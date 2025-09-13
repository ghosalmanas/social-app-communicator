package com.wtf.app.util;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe holder for AutomationContext using TransmittableThreadLocal.
 * Ensures proper context propagation in thread pool scenarios.
 * 
 * <p>This class provides thread-local storage of AutomationContext with the following features:
 * <ul>
 *   <li>Thread-safe context management</li>
 *   <li>Support for async context propagation</li>
 *   <li>Reference counting for context cleanup</li>
 *   <li>Detailed logging for debugging</li>
 * </ul>
 */
public final class ThreadLocalAutomationContext {
    private static final Logger logger = LoggerFactory.getLogger(ThreadLocalAutomationContext.class);
    
    // Using TransmittableThreadLocal for proper context propagation in thread pools
    private static final TransmittableThreadLocal<AutomationContext> contextHolder = 
        new TransmittableThreadLocal<>();
    
    // Counter for tracking context usage (useful for debugging and leak detection)
    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final Set<AutomationContext> contextList = new LinkedHashSet<>();
    
    // Maximum number of context clears before forcing removal
    private static final int MAX_CLEAR_ATTEMPTS = 5;

    private ThreadLocalAutomationContext() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Gets the current thread's AutomationContext.
     * 
     * @return the current AutomationContext, or null if none is set
     */
    public static AutomationContext getContext() {
        return contextHolder.get();
    }

    /**
     * Sets the AutomationContext for the current thread.
     * If null is passed, clears the current context.
     * 
     * @param context the AutomationContext to set, or null to clear
     */
    public static void setContext(AutomationContext context) {
        if (context == null) {
            clear();
            return;
        }
        
        try {
            contextHolder.set(context);
            counter.set(0); // Reset counter on new context
            logger.debug("Set context for task: {} (Type: {})", 
                context.getTaskId(), context.getTaskType());
        } catch (Exception e) {
            logger.error("Failed to set context for task: {}", context.getTaskId(), e);
            throw new IllegalStateException("Failed to set AutomationContext", e);
        }
    }

    /**
     * Clears the current thread's AutomationContext.
     * Implements a safety counter to prevent memory leaks.
     */
    public static void clear() {
        AutomationContext context = getContext();
        if (context == null) {
            return; // Already cleared
        }
        
        try {
            String taskId = context.getTaskId();
            int currentCount = counter.incrementAndGet();
            
            if (currentCount >= MAX_CLEAR_ATTEMPTS) {
                logger.debug("Force removing context after {} clear attempts for task: {}", 
                    currentCount, taskId);
                contextHolder.remove();
                counter.set(0);
                logger.debug("Successfully cleared context for task: {}", taskId);
            } else {
                logger.trace("Clear attempt {} for task: {}", currentCount, taskId);
            }
        } catch (Exception e) {
            logger.error("Error clearing AutomationContext", e);
            // Ensure we still try to remove the context
            contextHolder.remove();
            counter.set(0);
        }
    }

    /**
     * Creates and sets a new AutomationContext for the specified task type.
     * 
     * @param taskType the type of task to create context for
     * @return the newly created AutomationContext
     * @throws IllegalArgumentException if taskType is null
     */
    public static AutomationContext createContext(TaskType taskType) {
        Objects.requireNonNull(taskType, "TaskType cannot be null");
        AutomationContext ctx = new AutomationContext(taskType);
        contextList.add(ctx);
        setContext(ctx);
        return ctx;
    }

    /**
     * Gets the current clear attempt counter value.
     * Mainly useful for testing and debugging.
     * 
     * @return the current counter value
     */
    public static int getClearAttempts() {
        return counter.get();
    }
    
    /**
     * Resets the clear attempt counter.
     * Use with caution - primarily for testing.
     */
    static void resetCounter() {
        counter.set(0);
    }

    public  static Set<AutomationContext> getAllContexts(){
        return contextList;
    }
}
