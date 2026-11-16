package com.ecommerce.ecom_backend.config;

import com.ecommerce.ecom_backend.api.ApiVersion;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ErrorResponseOpenApiCustomizer implements OpenApiCustomizer {

    private static final String ERROR_SCHEMA = "ApiErrorResponse";
    private static final String EXAMPLE_TIMESTAMP = "2026-11-16T14:00:00Z";

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        openApi.getComponents().addSchemas(ERROR_SCHEMA, errorSchema());

        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            addIfAbsent(responses, "400", "Validation failed or malformed request",
                    errorExample("Validation failed", "VALIDATION_ERROR",
                            Map.of("name", "Brand name is required"), path));
            addIfAbsent(responses, "500", "Unexpected internal error",
                    errorExample("An unexpected error occurred", "INTERNAL_ERROR", null, path));

            if (isSecured(path, operation)) {
                addIfAbsent(responses, "401", "Missing, invalid or expired token",
                        errorExample("Authentication is required", "UNAUTHORIZED", null, path));
                addIfAbsent(responses, "403", "Authenticated user lacks the required role",
                        errorExample("Access denied", "FORBIDDEN", null, path));
            }
        }));
    }

    private boolean isSecured(String path, Operation operation) {
        if (operation.getSecurity() != null && operation.getSecurity().isEmpty()) {
            return false;
        }
        return !path.startsWith(ApiVersion.V1_AUTH) || path.equals(ApiVersion.V1_AUTH + "/me");
    }

    private void addIfAbsent(ApiResponses responses, String code, String description,
                             Map<String, Object> example) {
        if (responses.containsKey(code)) {
            return;
        }
        Content content = new Content().addMediaType("application/json",
                new MediaType()
                        .schema(new Schema<>().$ref(ERROR_SCHEMA))
                        .example(example));
        responses.addApiResponse(code, new ApiResponse().description(description).content(content));
    }

    private Map<String, Object> errorExample(String message, String errorCode, Object data, String path) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("success", false);
        example.put("message", message);
        example.put("errorCode", errorCode);
        example.put("data", data);
        example.put("timestamp", EXAMPLE_TIMESTAMP);
        example.put("path", path);
        return example;
    }

    private Schema<?> errorSchema() {
        return new ObjectSchema()
                .addProperty("success", new BooleanSchema().example(false))
                .addProperty("message", new StringSchema().example("Resource not found"))
                .addProperty("errorCode", new StringSchema().example("RESOURCE_NOT_FOUND"))
                .addProperty("data", new ObjectSchema().nullable(true)
                        .description("Field-level validation errors for VALIDATION_ERROR, otherwise null"))
                .addProperty("timestamp", new DateTimeSchema().example(EXAMPLE_TIMESTAMP))
                .addProperty("path", new StringSchema().example("/api/v1/products/999"));
    }
}
