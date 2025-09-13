package com.wtf.app.exception;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception thrown when validation of an object fails.
 */
public class ValidationException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 24L; // 24 hours in a day, perfect for validation exceptions
    
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        this(message, null, null);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        this(message, fieldErrors, null);
    }

    public ValidationException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ValidationException(String message, Map<String, String> fieldErrors, Throwable cause) {
        super(message, cause);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors != null ? new LinkedHashMap<>(fieldErrors) : Map.of();
    }
    
    @Override
    public String toString() {
        return String.format("""
            ValidationException {
                message=%s,
                errorCode=%s,
                fieldErrors=%s,
                cause=%s
            }""", 
            getMessage(),
            getErrorCode(),
            fieldErrors,
            getCause()
        );
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
}
