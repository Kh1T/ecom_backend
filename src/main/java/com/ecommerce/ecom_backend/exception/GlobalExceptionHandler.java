package com.ecommerce.ecom_backend.exception;

import com.ecommerce.ecom_backend.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        return toResponse(errorCode.getStatus(), errorCode, ex.getMessage(), null,
                request.getRequestURI(), HttpHeaders.EMPTY);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return toResponse(ErrorCode.UNAUTHORIZED.getStatus(), ErrorCode.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED.getDefaultMessage(), null, request.getRequestURI(), HttpHeaders.EMPTY);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return toResponse(ErrorCode.FORBIDDEN.getStatus(), ErrorCode.FORBIDDEN,
                ErrorCode.FORBIDDEN.getDefaultMessage(), null, request.getRequestURI(), HttpHeaders.EMPTY);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation on {} {}", request.getMethod(), request.getRequestURI());
        return toResponse(ErrorCode.DUPLICATE_RESOURCE.getStatus(), ErrorCode.DUPLICATE_RESOURCE,
                ErrorCode.DUPLICATE_RESOURCE.getDefaultMessage(), null, request.getRequestURI(), HttpHeaders.EMPTY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return toResponse(ErrorCode.INTERNAL_ERROR.getStatus(), ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(), null, request.getRequestURI(), HttpHeaders.EMPTY);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return toResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), fieldErrors, path(request), headers);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ErrorCode errorCode = resolveErrorCode(status);
        return toResponse(status, errorCode, errorCode.getDefaultMessage(), null, path(request), headers);
    }

    private ErrorCode resolveErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.MALFORMED_REQUEST;
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            case CONTENT_TOO_LARGE -> ErrorCode.PAYLOAD_TOO_LARGE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    private String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    private ResponseEntity<Object> toResponse(HttpStatus status, ErrorCode errorCode, String message,
                                              Object data, String path, HttpHeaders headers) {
        ApiErrorResponse body = ApiErrorResponse.of(message, errorCode.name(), data, path);
        return ResponseEntity.status(status).headers(headers).body(body);
    }
}
