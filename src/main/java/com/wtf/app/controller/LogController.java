package com.wtf.app.controller;

// Java imports
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Spring imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Jakarta imports
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private final String logFilePath;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static final long LOG_POLL_INTERVAL_MS = 1000; // 1 second
    private static final long SSE_TIMEOUT_MS = 2 * 60 * 60 * 1000; // 2 hours
    private static final String LOG_EVENT_NAME = "log"; // This matches the event name the frontend is listening for
    private final AtomicLong lastFileSize = new AtomicLong(0);

    public LogController(@Value("${logging.file.name:./logs/application.log}") String logFilePath) {
        this.logFilePath = logFilePath;
        logger.info("Initializing LogController with log file: {}", new File(logFilePath).getAbsolutePath());
        initializeLogFile();
        startLogMonitor();
    }

    private void initializeLogFile() {
        try {
            File logFile = new File(logFilePath);
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            lastFileSize.set(logFile.length());
        } catch (Exception e) {
            logger.error("Error initializing log file", e);
        }
    }

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "*")
    public SseEmitter streamLogs(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream");
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AtomicBoolean completed = new AtomicBoolean(false);

        // Send initial ping
        sendPing(emitter);

        // Schedule log polling
        ScheduledFuture<?> logPoller = scheduler.scheduleAtFixedRate(
            () -> checkForNewLogs(emitter, completed),
            0, LOG_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS
        );

        // Set up cleanup
        Runnable cleanup = () -> {
            if (completed.compareAndSet(false, true)) {
                logPoller.cancel(true);
                if (true) {  // Always try to complete, let the emitter handle its state
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onError(e -> {
            if (!isClientDisconnectedError(e)) {
                logger.warn("SSE error: {}", e.getMessage());
            }
            cleanup.run();
        });
        emitter.onTimeout(cleanup);

        return emitter;
    }

    private void startLogMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                File logFile = new File(logFilePath);
                if (!logFile.exists()) return;

                long currentSize = logFile.length();
                long lastSize = lastFileSize.get();

                if (currentSize < lastSize) {
                    // Log file was rotated or truncated
                    lastFileSize.set(0);
                    lastSize = 0;
                }


                if (currentSize > lastSize) {
                    try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
                        raf.seek(lastSize);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            logQueue.add(line);
                        }
                        lastFileSize.set(raf.getFilePointer());
                    } catch (Exception e) {
                        logger.error("Error reading log file", e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in log monitor", e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void checkForNewLogs(SseEmitter emitter, AtomicBoolean completed) {
        if (completed.get()) return;

        try {
            int batchSize = 100;
            while (batchSize-- > 0 && !logQueue.isEmpty() && !completed.get()) {
                String line = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (line != null && !line.trim().isEmpty()) {
                    try {
                        emitter.send(SseEmitter.event()
                            .name(LOG_EVENT_NAME)
                            .data(line + "\n")
                            .reconnectTime(3000));
                    } catch (IOException e) {
                        if (!isClientDisconnectedError(e)) {
                            logger.warn("Error sending log line: {}", e.getMessage());
                            logQueue.put(line);
                        }
                        break;
                    }
                }
            }

            // Send ping to keep connection alive
            if (!completed.get()) {
                sendPing(emitter);
            }
        } catch (Exception e) {
            if (!completed.get() && !isClientDisconnectedError(e)) {
                logger.warn("Error in log polling: {}", e.getMessage());
            }
        }
    }

    private void sendPing(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                .id(String.valueOf(System.currentTimeMillis()))
                .name("ping")
                .data("ping")
                .reconnectTime(3000));
        } catch (Exception e) {
            if (!isClientDisconnectedError(e)) {
                logger.warn("Error sending ping: {}", e.getMessage());
            }
        }
    }

    private boolean isClientDisconnectedError(Throwable e) {
        if (e == null) return false;
        String message = e.getMessage();
        return message != null && (
            message.contains("Broken pipe") ||
            message.contains("Connection reset") ||
            message.contains("Software caused connection abort") ||
            e instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException
        ) || (e.getCause() != null && isClientDisconnectedError(e.getCause()));
    }
}
