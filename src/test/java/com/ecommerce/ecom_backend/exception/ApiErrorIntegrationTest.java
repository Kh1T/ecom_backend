package com.ecommerce.ecom_backend.exception;

import com.ecommerce.ecom_backend.security.CustomUserDetailsService;
import com.ecommerce.ecom_backend.security.JwtUtil;
import com.ecommerce.ecom_backend.service.AdminBrandService;
import com.ecommerce.ecom_backend.service.AdminCategoryService;
import com.ecommerce.ecom_backend.service.AdminProductService;
import com.ecommerce.ecom_backend.service.AuthService;
import com.ecommerce.ecom_backend.service.OrderService;
import com.ecommerce.ecom_backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiErrorIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String CUSTOMER_EMAIL = "customer@example.com";
    private static final String GHOST_EMAIL = "ghost@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AdminProductService adminProductService;

    @MockitoBean
    private AdminBrandService adminBrandService;

    @MockitoBean
    private AdminCategoryService adminCategoryService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private AuthService authService;

    private UserDetails admin;
    private UserDetails customer;

    @BeforeEach
    void setUp() {
        admin = User.withUsername(ADMIN_EMAIL).password("unused").authorities("ROLE_ADMIN").build();
        customer = User.withUsername(CUSTOMER_EMAIL).password("unused").authorities("ROLE_CUSTOMER").build();
        when(userDetailsService.loadUserByUsername(ADMIN_EMAIL)).thenReturn(admin);
        when(userDetailsService.loadUserByUsername(CUSTOMER_EMAIL)).thenReturn(customer);
    }

    private String bearer(UserDetails user) {
        return "Bearer " + jwtUtil.generateAccessToken(user);
    }

    @Test
    void notFoundReturnsStandardErrorFormat() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/products/999"));
    }

    @Test
    void duplicateBrandReturnsConflict() throws Exception {
        when(adminBrandService.createBrand(any())).thenThrow(new DuplicateResourceException("Brand already exists"));

        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(AUTHORIZATION, bearer(admin))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Brand already exists"));
    }

    @Test
    void validationErrorReturnsFieldErrorsInData() throws Exception {
        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(AUTHORIZATION, bearer(admin))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.name").value("Brand name is required"));
    }

    @Test
    void malformedJsonReturnsBadRequestWithoutParserDetails() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(APPLICATION_JSON)
                        .content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Malformed request"))
                .andExpect(content().string(not(containsString("JSON"))));
    }

    @Test
    void invalidCredentialsReturnUnauthorizedWithoutEchoingPassword() throws Exception {
        when(authService.signIn(any())).thenThrow(
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(content().string(not(containsString("wrong-pass"))));
    }

    @Test
    void missingTokenReturnsUnauthorizedInStandardFormat() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    @Test
    void invalidTokenReturnsUnauthorizedInStandardFormat() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header(AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void tokenOfDeletedUserReturnsUnauthorizedInStandardFormat() throws Exception {
        UserDetails ghost = User.withUsername(GHOST_EMAIL).password("unused").authorities("ROLE_CUSTOMER").build();
        String ghostToken = "Bearer " + jwtUtil.generateAccessToken(ghost);
        when(userDetailsService.loadUserByUsername(GHOST_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found with email: " + GHOST_EMAIL));

        mockMvc.perform(get("/api/v1/orders").header(AUTHORIZATION, ghostToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(content().string(not(containsString(GHOST_EMAIL))));
    }

    @Test
    void insufficientRoleReturnsForbiddenInStandardFormat() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories").header(AUTHORIZATION, bearer(customer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void businessRuleViolationReturnsUnprocessableEntity() throws Exception {
        when(orderService.createOrder(eq(CUSTOMER_EMAIL), any())).thenThrow(
                new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Insufficient stock for product: Phone"));

        mockMvc.perform(post("/api/v1/orders")
                        .header(AUTHORIZATION, bearer(customer))
                        .contentType(APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Insufficient stock for product: Phone"));
    }

    @Test
    void wrongHttpMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/products").header(AUTHORIZATION, bearer(admin)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void unknownRouteReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/unknown").header(AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void unexpectedErrorReturnsGenericInternalErrorWithoutLeakingDetails() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new RuntimeException("SELECT * FROM products WHERE secret = 1"));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("SELECT"))));
    }

    @Test
    void successfulResponseFormatIsUnchanged() throws Exception {
        when(adminCategoryService.getCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/categories").header(AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void openApiDocumentsErrorResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders'].get.responses['403']").exists());
    }
}
