package com.wtf.app.exception;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Base class for all business-related exceptions in the application.
 */
public class BusinessException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L; // The answer to life, the universe, and everything
    
    private final String errorCode;
    private final transient Object[] args; // Marked as transient as Object[] might not be serializable
    private final LocalDateTime timestamp;
    private final transient Object[] arguments; // Marked as transient as Object[] might not be serializable

    public BusinessException(String message) {
        this(message, (String) null, (Throwable) null, new Object[0]);
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, null, new Object[0]);
    }

    public BusinessException(String message, Throwable cause) {
        this(message, null, cause, new Object[0]);
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        this(message, errorCode, cause, new Object[0]);
    }

    public BusinessException(String message, String errorCode, Object... args) {
        this(message, errorCode, null, args);
    }
    
    public BusinessException(String message, String errorCode, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.arguments = args != null ? args : new Object[0];
        this.args = this.arguments; // Keep backward compatibility
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return arguments != null ? arguments.clone() : new Object[0];
    }
    
    @Override
    public String toString() {
        return String.format("""
            BusinessException {
                message=%s,
                errorCode=%s,
                timestamp=%s,
                args=%s,
                cause=%s
            }""", 
            getMessage(), 
            errorCode, 
            timestamp,
            args != null ? Arrays.deepToString(args) : "null",
            getCause()
        );
    }
}
