package com.wtf.app.exception;

import com.wtf.app.util.ApiResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.web.server.ResponseStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that provides centralized exception handling across all @RequestMapping methods.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    // 400 - Bad Request
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        logger.warn("Argument type mismatch: {}", ex.getMessage());
        return ApiResponseUtil.error("Invalid argument type",
                                  String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                                              ex.getName(), ex.getValue(), 
                                              ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"),
                                  HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return ApiResponseUtil.error("Invalid argument", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public final ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex, WebRequest request) {
        logger.warn("Illegal state: {}", ex.getMessage());
        return ApiResponseUtil.error("Invalid state", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ServletException.class)
    public final ResponseEntity<Map<String, Object>> handleAuthenticationException(ServletException ex, WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return ApiResponseUtil.error("Authentication failed", "Invalid or missing authentication credentials", 
                                  HttpStatus.UNAUTHORIZED);
    }

    // 403 - Forbidden
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, org.springframework.http.HttpHeaders headers, 
            HttpStatus status, WebRequest request) {
        if (ex instanceof org.springframework.web.multipart.MaxUploadSizeExceededException) {
            logger.warn("File size exceeded: {}", ex.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.put("error", "File too large");
            response.put("message", "File size exceeds the maximum allowed limit");
            return new ResponseEntity<>(response, headers, HttpStatus.PAYLOAD_TOO_LARGE);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public final ResponseEntity<Map<String, Object>> handleAuthorizationException(ForbiddenException ex, WebRequest request) {
        logger.warn("Authorization failed: {}", ex.getMessage());
        return ApiResponseUtil.forbidden(ex.getMessage());
    }

    // 404 - Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ApiResponseUtil.error("Resource not found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public final ResponseEntity<Map<String, Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, WebRequest request) {
        logger.warn("Resource already exists: {}", ex.getMessage());
        return ApiResponseUtil.error("Resource exists", ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 415 - Unsupported Media Type
    // Handled by parent class ResponseEntityExceptionHandler
    
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, org.springframework.http.HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        logger.warn("Message not readable: {}", ex.getMessage());
        return new ResponseEntity<>(
            ApiResponseUtil.error(
                "Malformed request",
                "The request body is not valid",
                HttpStatus.BAD_REQUEST
            ).getBody(),
            headers,
            HttpStatus.BAD_REQUEST
        );
    }

    // 422 - Unprocessable Entity
    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex, WebRequest request) {
        Map<String, String> errors = ex.hasFieldErrors() ? ex.getFieldErrors() : new HashMap<>();
        logger.warn("Validation failed: {}", errors);
        return ApiResponseUtil.validationError(ex.getMessage(), errors);
    }

    // 429 - Too Many Requests
    // Note: Rate limiting should be handled by Spring's built-in rate limiting or a filter

    // 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ApiResponseUtil.error("An unexpected error occurred", "Please try again later", 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 503 - Service Unavailable
    @ExceptionHandler(ResponseStatusException.class)
    public final ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        logger.error("Response status exception: {}", ex.getReason(), ex);
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ApiResponseUtil.error("Request failed", ex.getReason() != null ? ex.getReason() : "An error occurred", status);
    }

    // BindException is handled by the parent class ResponseEntityExceptionHandler
    // through the handleMethodArgumentNotValid method

    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(ApiResponseUtil.validationError("Validation failed", errors).getBody(), headers, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, org.springframework.http.HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t).append(" "));
        
        return new ResponseEntity<>(ApiResponseUtil.error("Method not allowed", builder.toString(), HttpStatus.METHOD_NOT_ALLOWED).getBody(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, org.springframework.http.HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        
        return new ResponseEntity<>(ApiResponseUtil.error("Unsupported media type", builder.toString(), HttpStatus.UNSUPPORTED_MEDIA_TYPE).getBody(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        logger.warn("Constraint violation: {}", errors);
        return ApiResponseUtil.validationError("Validation failed", errors);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);
        return ApiResponseUtil.error("An unexpected error occurred", "Please try again later", 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
