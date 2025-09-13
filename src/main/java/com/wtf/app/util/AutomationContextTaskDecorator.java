package com.wtf.app.util;

import com.wtf.app.model.dto.AutomationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Task decorator that propagates the AutomationContext to async task execution.
 * Ensures proper context management and cleanup for asynchronous operations.
 */
/**
 * Task decorator that propagates AutomationContext to async task execution.
 * Uses TransmittableThreadLocal for proper context propagation across thread pools.
 */
@Component
@SuppressWarnings("NullableProblems")
public class AutomationContextTaskDecorator implements TaskDecorator {
    private static final Logger logger = LoggerFactory.getLogger(AutomationContextTaskDecorator.class);
    private static final String TASK_START = "Starting async task execution";
    private static final String TASK_COMPLETE = "Completed async task execution";
    private static final String CONTEXT_PROPAGATION_ERROR = "Error propagating AutomationContext to async task";
    
    // Metrics counters
    private final AtomicLong tasksExecuted = new AtomicLong(0);
    private final AtomicLong contextPropagationFailures = new AtomicLong(0);
    private final AtomicLong taskExecutionFailures = new AtomicLong(0);

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable task cannot be null");
        
        // Capture the current context at decoration time (parent thread)
        final AutomationContext parentContext = AutomationContextHolder.getContext();
        final long startTime = System.currentTimeMillis();
        
        return () -> {
            // This runs in the async task's thread
            final Thread currentThread = Thread.currentThread();
            final String threadName = currentThread.getName();
            final String taskId = parentContext != null ? parentContext.getTaskId() : "NO_CONTEXT";
            final AtomicReference<AutomationContext> taskContext = new AtomicReference<>();
            
            try {
                logger.debug("{} [Task:{}] [Thread:{}]", TASK_START, taskId, threadName);
                
                // Set up context for the task
                if (parentContext != null) {
                    try {
                        // Create a defensive copy of the context to prevent shared state issues
                        AutomationContext contextCopy = new AutomationContext(parentContext.getTaskType());
                        contextCopy.setTaskSocialDataConst(parentContext.getTaskSocialDataConst());
                        
                        // Set the context in the current thread
                        ThreadLocalAutomationContext.setContext(contextCopy);
                        taskContext.set(contextCopy);
                        
                        logContextPropagation(contextCopy);
                    } catch (Exception e) {
                        contextPropagationFailures.incrementAndGet();
                        logger.error("{} [Task:{}] [Thread:{}] - {}", 
                            CONTEXT_PROPAGATION_ERROR, taskId, threadName, e.getMessage(), e);
                        throw new IllegalStateException(CONTEXT_PROPAGATION_ERROR, e);
                    }
                } else {
                    logger.debug("No parent context found for task [Thread: {}]", threadName);
                }
                
                // Execute the actual task
                try {
                    runnable.run();
                    tasksExecuted.incrementAndGet();
                    
                    if (logger.isDebugEnabled()) {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.debug("{} [Task:{}] [Thread:{}] completed in {} ms", 
                            TASK_COMPLETE, taskId, threadName, duration);
                    }
                } catch (Exception e) {
                    taskExecutionFailures.incrementAndGet();
                    logger.error("Task execution failed [Task:{}] [Thread:{}] - {}", 
                        taskId, threadName, e.getMessage(), e);
                    throw e;
                }
                
            } finally {
                // Always clean up the context, even if the task throws an exception
                try {
                    // Only clear if we set a context for this task
                    if (taskContext.get() != null) {
                        ThreadLocalAutomationContext.clear();
                        logger.trace("Cleaned up context for task [Task:{}] [Thread:{}]", 
                            taskId, threadName);
                    }
                } catch (Exception e) {
                    // Log but don't propagate - we don't want to mask any original exception
                    logger.error("Error cleaning up AutomationContext after task execution [Task:{}] [Thread:{}] - {}", 
                        taskId, threadName, e.getMessage(), e);
                }
            }
        };
    }
    
    /**
     * Logs detailed information about context propagation.
     * @param context The AutomationContext being propagated
     */
    private void logContextPropagation(AutomationContext context) {
        if (context == null) {
            logger.debug("No context provided for logging propagation details");
            return;
        }
        
        try {
            String taskId = context.getTaskId();
            String taskType = context.getTaskType() != null ? context.getTaskType().name() : "UNKNOWN";
            String socialType = "UNKNOWN";
            String taskDataTaskType = "UNKNOWN";
            
            if (context.getTaskSocialDataConst() != null) {
                socialType = context.getTaskSocialDataConst().getSocialType() != null 
                    ? context.getTaskSocialDataConst().getSocialType().name() 
                    : "UNSET";
                    
                taskDataTaskType = context.getTaskSocialDataConst().getTaskType() != null
                    ? context.getTaskSocialDataConst().getTaskType().name()
                    : "UNSET";
            }
                
            logger.debug("Propagated AutomationContext - Task[ID: {}, Type: {}, Social: {}, TaskType: {}] to thread: {}",
                taskId, taskType, socialType, taskDataTaskType, Thread.currentThread().getName());
                
        } catch (Exception e) {
            logger.warn("Error logging context propagation details: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Gets the number of tasks successfully executed by this decorator.
     * @return The count of successfully executed tasks
     */
    public long getTasksExecutedCount() {
        return tasksExecuted.get();
    }
    
    /**
     * Gets the number of context propagation failures.
     * @return The count of context propagation failures
     */
    public long getContextPropagationFailureCount() {
        return contextPropagationFailures.get();
    }
    
    /**
     * Gets the number of task execution failures.
     * @return The count of task execution failures
     */
    public long getTaskExecutionFailureCount() {
        return taskExecutionFailures.get();
    }
    
    /**
     * Resets all metrics counters to zero.
     * Useful for testing or when metrics need to be cleared.
     */
    public void resetMetrics() {
        tasksExecuted.set(0);
        contextPropagationFailures.set(0);
        taskExecutionFailures.set(0);
    }
}
