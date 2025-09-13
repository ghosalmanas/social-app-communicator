package com.wtf.app.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating consistent API responses.
 */
public class ApiResponseUtil {
    
    private ApiResponseUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a success response with data.
     */
    public static <T> ResponseEntity<Map<String, Object>> success(String message, T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Creates a success response without data.
     */
    public static ResponseEntity<Map<String, Object>> success(String message) {
        return success(message, null);
    }
    
    /**
     * Creates an error response.
     */
    public static ResponseEntity<Map<String, Object>> error(String message, String error, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Creates an error response from an exception.
     */
    public static ResponseEntity<Map<String, Object>> error(String message, Throwable throwable, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("error", throwable != null ? throwable.getMessage() : "Unknown error");
        response.put("timestamp", LocalDateTime.now());
        
        if (throwable != null && throwable.getCause() != null) {
            response.put("cause", throwable.getCause().getMessage());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Creates a validation error response.
     */
    public static ResponseEntity<Map<String, Object>> validationError(String message, Map<String, String> fieldErrors) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "VALIDATION_ERROR");
        response.put("message", message);
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Creates a not found response.
     */
    public static ResponseEntity<Map<String, Object>> notFound(String resource, String id) {
        return error("Resource not found", String.format("%s with id %s not found", resource, id), HttpStatus.NOT_FOUND);
    }
    
    /**
     * Creates an unauthorized response.
     */
    public static ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return error("Unauthorized", message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Creates a forbidden response.
     */
    public static ResponseEntity<Map<String, Object>> forbidden(String message) {
        return error("Forbidden", message, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Creates a bad request response.
     */
    public static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return error("Bad Request", message, HttpStatus.BAD_REQUEST);
    }
}
