package com.ecommerce.ecom_backend.exception;

import com.ecommerce.ecom_backend.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private static final String PATH = "/api/v1/test";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", PATH);
    }

    private ApiErrorResponse assertErrorBody(ResponseEntity<Object> response, HttpStatus status, String errorCode) {
        assertEquals(status, response.getStatusCode());
        ApiErrorResponse body = (ApiErrorResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.success());
        assertEquals(errorCode, body.errorCode());
        assertEquals(PATH, body.path());
        assertNotNull(body.timestamp());
        return body;
    }

    @Test
    void businessExceptionUsesStatusAndCodeFromErrorCode() {
        ResponseEntity<Object> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Only pending orders can be cancelled"),
                request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION");
        assertEquals("Only pending orders can be cancelled", body.message());
        assertNull(body.data());
    }

    @Test
    void resourceNotFoundReturnsNotFound() {
        ResponseEntity<Object> response = handler.handleBusinessException(
                new ResourceNotFoundException("Product not found"), request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        assertEquals("Product not found", body.message());
    }

    @Test
    void duplicateResourceReturnsConflict() {
        ResponseEntity<Object> response = handler.handleBusinessException(
                new DuplicateResourceException("Brand already exists"), request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
        assertEquals("Brand already exists", body.message());
    }

    @Test
    void authenticationErrorUsesGenericMessageWithoutLeakingDetails() {
        ResponseEntity<Object> response = handler.handleAuthenticationException(
                new BadCredentialsException("secret-internal-detail"), request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        assertEquals("Authentication is required", body.message());
    }

    @Test
    void accessDeniedReturnsForbiddenWithGenericMessage() {
        ResponseEntity<Object> response = handler.handleAccessDeniedException(
                new AccessDeniedException("role details"), request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.FORBIDDEN, "FORBIDDEN");
        assertEquals("Access denied", body.message());
    }

    @Test
    void dataIntegrityViolationReturnsConflictWithoutDatabaseDetails() {
        ResponseEntity<Object> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"uk_products_sku\""),
                request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
        assertEquals("Resource already exists", body.message());
        assertFalse(body.message().contains("constraint"));
    }

    @Test
    void maxUploadSizeReturnsPayloadTooLarge() throws Exception {
        ResponseEntity<Object> response = handler.handleException(
                new MaxUploadSizeExceededException(5L), new ServletWebRequest(request()));

        assertErrorBody(response, HttpStatus.CONTENT_TOO_LARGE, "PAYLOAD_TOO_LARGE");
    }

    @Test
    void unexpectedErrorReturnsGenericInternalErrorWithoutLeakingDetails() {
        ResponseEntity<Object> response = handler.handleUnexpectedException(
                new RuntimeException("SELECT * FROM users WHERE password_hash = 'x'"), request());

        ApiErrorResponse body = assertErrorBody(response, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        assertEquals("An unexpected error occurred", body.message());
        assertFalse(body.message().contains("SELECT"));
    }
}
