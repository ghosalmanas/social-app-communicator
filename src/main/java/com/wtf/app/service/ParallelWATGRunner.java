package com.wtf.app.service;

import com.wtf.app.parent.WATGCommonUtils;
import com.wtf.app.parent.WATGParent;
import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.util.ThreadLocalAutomationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for running multiple WATGParent instances in parallel with proper resource management.
 */
@Service
public class ParallelWATGRunner {
    private static final Logger logger = LogManager.getLogger(ParallelWATGRunner.class);
    private final ThreadPoolTaskExecutor taskExecutor;
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public ParallelWATGRunner(@Qualifier("automationTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        logger.info("Initialized ParallelWATGRunner with thread pool size: {}-{}", 
            taskExecutor.getCorePoolSize(), taskExecutor.getMaxPoolSize());
    }

    /**
     * Run multiple WATGParent instances in parallel.
     * @param instances List of WATGCommonUtils instances to run
     * @return List of CompletableFuture representing each task's completion
     */
    public List<CompletableFuture<Void>> runInParallel(List<WATGCommonUtils> instances) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (WATGCommonUtils instance : instances) {
            if (instance instanceof WATGParent) {
                WATGParent watgParent = (WATGParent) instance;
                CompletableFuture<Void> future = executeWATGTask(watgParent);
                futures.add(future);
            } else {
                logger.warn("Skipping non-WATGParent instance: {}", instance.getClass().getName());
            }
        }
        
        return futures;
    }

    /**
     * Execute a single WATGParent task with proper context and error handling.
     */
    private CompletableFuture<Void> executeWATGTask(WATGParent watgParent) {
        int taskId = taskCounter.incrementAndGet();
        String threadName = "WATGTask-" + taskId;
        
        return CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName(threadName);
            ThreadContext.push(threadName);
            
            try (AutomationContext ignored = ThreadLocalAutomationContext.getContext()) {
                logger.info("Starting WATGParent task {}", taskId);
                watgParent.setTaskTypeMapping(ignored.getTaskType(),watgParent);
                watgParent.traverseGroupsTemplate(watgParent);
                logger.info("Completed WATGParent task {}", taskId);
            } catch (Exception e) {
                logger.error("Error in WATGParent task " + taskId, e);
                throw new RuntimeException("Task " + taskId + " failed", e);
            } finally {
                try {
                    watgParent.quitDriver();
                } catch (Exception e) {
                    logger.error("Error cleaning up WATGParent task " + taskId, e);
                }
                ThreadContext.pop();
                ThreadContext.clearStack();
            }
        }, taskExecutor).whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("WATGParent task {} completed with error", taskId, ex);
            } else {
                logger.info("WATGParent task {} completed successfully", taskId);
            }
        });
    }
    
    /**
     * Shutdown the task executor and wait for tasks to complete.
     */
    public void shutdown() {
        logger.info("Initiating graceful shutdown of ParallelWATGRunner");
        taskExecutor.shutdown();
        logger.info("ParallelWATGRunner shutdown completed");
    }
}
