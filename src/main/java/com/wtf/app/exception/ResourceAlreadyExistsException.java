package com.wtf.app.exception;

import java.io.Serial;

/**
 * Exception thrown when trying to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 409L; // HTTP 409 Conflict - perfect for this exception
    
    private final String resourceName;
    private final String identifier;

    public ResourceAlreadyExistsException(String resourceName, String identifier) {
        super(String.format("%s already exists with identifier: %s", resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceAlreadyExistsException(String resourceName, String identifier, String message) {
        super(message);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceAlreadyExistsException(String resourceName, String identifier, String message, Throwable cause) {
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
            ResourceAlreadyExistsException {
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
