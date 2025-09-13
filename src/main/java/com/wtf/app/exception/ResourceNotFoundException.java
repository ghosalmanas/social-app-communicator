package com.wtf.app.exception;

import java.io.Serial;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 404L; // HTTP 404 Not Found - perfect for this exception
    
    private final String resourceName;
    private final String identifier;

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceNotFoundException(String resourceName, String identifier, String message) {
        super(message);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceNotFoundException(String resourceName, String identifier, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    @Override
    public String toString() {
        return String.format("""
            ResourceNotFoundException {
                message=%s,
                resourceName=%s,
                identifier=%s,
                cause=%s
            }""", 
            getMessage(),
            resourceName,
            identifier,
            getCause()
        );
    }
}
