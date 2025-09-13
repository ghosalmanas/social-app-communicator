package com.wtf.app.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);
    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request, WebRequest webRequest) {
        // Create a basic error response
        Map<String, Object> responseBody = new HashMap<>();
        
        // Get the status code from the request
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        
        // Get the original request URI
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path == null) {
            path = request.getRequestURI();
        }
        
        // Check if it's a static resource request
        if (path != null && (path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".png") || path.endsWith(".jpg"))) {
            // Skip logging for specific known endpoints
            if (!"/api-dashboard.html".equals(path)) {
                logger.debug("Static resource not found: {}", path);
            }
            statusCode = HttpStatus.NOT_FOUND.value();
            responseBody.put("status", statusCode);
            responseBody.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
            responseBody.put("message", "The requested resource was not found");
            responseBody.put("path", path);
            responseBody.put("timestamp", System.currentTimeMillis());
            // Skip warning for specific known endpoints
            if (!"/api-dashboard.html".equals(path)) {
                logger.warn("Static resource not found: {}", path);
            }
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }
        
        // Get the error attributes
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest);
        
        // Set response fields
        responseBody.put("status", statusCode);
        responseBody.put("error", errorAttributes.get("error") != null ? 
            errorAttributes.get("error") : HttpStatus.valueOf(statusCode).getReasonPhrase());
        responseBody.put("message", errorAttributes.get("message") != null ? 
            errorAttributes.get("message") : getErrorMessage(statusCode));
        responseBody.put("path", errorAttributes.get("path") != null ? 
            errorAttributes.get("path") : path);
        responseBody.put("timestamp", System.currentTimeMillis());
        
        // Log the error if it's a server error
        if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            logger.error("Server error occurred: {}", responseBody);
        } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
            logger.warn("Resource not found: {}", path);
        } else {
            logger.warn("Client error occurred: {}", responseBody);
        }
        
        return new ResponseEntity<>(responseBody, HttpStatus.valueOf(statusCode));
    }
    
    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return errorAttributes.getErrorAttributes(
            webRequest,
            ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.BINDING_ERRORS
            )
        );
    }

    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 404 -> "The requested resource was not found";
            case 403 -> "You don't have permission to access this resource";
            case 401 -> "Authentication is required to access this resource";
            case 400 -> "The request was invalid or cannot be served";
            default -> "An unexpected error occurred";
        };
    }
}
