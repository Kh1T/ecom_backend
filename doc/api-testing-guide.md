# API Testing Guide

## Environment variables

This project loads variables from a `.env` file in the project root using Spring Boot's native config import.

1. Copy `.env.example` to `.env`:

   ```bash
   cp .env.example .env
   ```

2. Fill in your real database credentials, JWT secret, and Cloudinary credentials.
3. Restart the application.

The `.env` file is ignored by Git, so your secrets stay local.

---

## Authentication

This backend uses **JWT Bearer tokens**. Most endpoints require an `Authorization` header.

### Public endpoints (no token needed)

- `POST /api/auth/sign-up`
- `POST /api/auth/sign-in`
- `POST /api/auth/refresh`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- Swagger UI: `/swagger-ui.html`

### Protected endpoints

All non-public endpoints require:

```http
Authorization: Bearer <access_token>
```

The token type must be `ACCESS`. Refresh tokens will be rejected.

Role requirements:

| Endpoint prefix | Required role |
|-----------------|---------------|
| `/api/v1/products/**` (write) | `ADMIN` |
| `/api/v1/orders/**` | `CUSTOMER` or `ADMIN` |
| `/api/v1/admin/**` | `ADMIN` or `CATALOG_MANAGER` |

---

## How to get a token

### 1. Sign in

```http
POST /api/auth/sign-in
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

Seeded test users:

| Email | Password | Role |
|-------|----------|------|
| `admin@example.com` | `admin123` | ADMIN |
| `john@example.com` | `password123` | CUSTOMER |
| `jane@example.com` | `password123` | CUSTOMER |

### 2. Use the `accessToken` from the response

Example response:

```json
{
  "id": 2,
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER",
  "message": "Signed in successfully",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## Product APIs

### Public (no token needed)

```http
GET /api/v1/products?page=0&size=10&sort=id,asc
GET /api/v1/products/{id}
GET /api/v1/products/search?q=phone
```

## Admin APIs

All `/api/v1/admin/**` endpoints require `ADMIN` or `CATALOG_MANAGER` role.

Use the seeded admin user to get a token:

```http
POST /api/auth/sign-in
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

### Products

```http
POST /api/v1/admin/products
PUT /api/v1/admin/products/{id}
PATCH /api/v1/admin/products/{id}/status
DELETE /api/v1/admin/products/{id}
POST /api/v1/admin/products/{id}/images
DELETE /api/v1/admin/products/{id}/images/{imageId}
POST /api/v1/admin/products/{id}/variants
PATCH /api/v1/admin/products/{id}/variants/{variantId}
```

Example upload image (admin token required):

```http
POST /api/v1/admin/products/1/images
Content-Type: multipart/form-data
Authorization: Bearer <admin_access_token>

file: <select image file>
sortOrder: 0
```

Images are uploaded to **Cloudinary** and the returned `secure_url` is stored in the database.

Example create product:

```http
POST /api/v1/admin/products
Content-Type: application/json
Authorization: Bearer <admin_access_token>

{
  "name": "Gaming Headset",
  "description": "Wireless gaming headset with noise cancelling mic",
  "price": 79.99,
  "stockQuantity": 40,
  "sku": "ELEC-005",
  "categoryId": 1,
  "brandId": 1
}
```

Example status update:

```http
PATCH /api/v1/admin/products/1/status
Content-Type: application/json
Authorization: Bearer <admin_access_token>

{
  "status": "ARCHIVED"
}
```

### Categories

```http
POST /api/v1/admin/categories
PUT /api/v1/admin/categories/{id}
```

### Brands

```http
POST /api/v1/admin/brands
```

---

## Customer Order APIs

All order endpoints are under `/api/v1/orders` and require a Bearer token with the **`CUSTOMER`** role (ADMIN also allowed).

### Create an order

```http
POST /api/v1/orders
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

Rules:

- `productId` must exist.
- `quantity` must be at least 1.
- Stock must be sufficient.
- Duplicate `productId` entries are merged automatically.

### Get order history

```http
GET /api/v1/orders
Authorization: Bearer <access_token>
```

Returns only the logged-in customer's orders, newest first.

### View order details

```http
GET /api/v1/orders/{orderId}
Authorization: Bearer <access_token>
```

### Cancel an order

```http
POST /api/v1/orders/{orderId}/cancel
Authorization: Bearer <access_token>
```

Only orders with status `PENDING` can be cancelled. Stock is restored automatically.

### View shipment tracking

```http
GET /api/v1/orders/{orderId}/tracking
Authorization: Bearer <access_token>
```

### Confirm delivery

```http
POST /api/v1/orders/{orderId}/confirm-delivery
Authorization: Bearer <access_token>
```

Allowed for orders with status `PENDING` or `SHIPPED`.

---

## Common errors

| HTTP | Cause |
|------|-------|
| `403 Forbidden` | Missing `Authorization` header, wrong token format, expired token, using a refresh token instead of access token, or role not allowed for the endpoint. |
| `400 Bad Request` | Validation failed (e.g., empty items, quantity < 1). |
| `404 Not Found` | Resource does not exist or does not belong to the user. |
| `409` / runtime error | Insufficient stock, order already shipped/cancelled, SKU already exists, etc. |

---

## Swagger

You can also explore and test the APIs through Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

### How to authenticate in Swagger UI

1. Open Swagger UI in your browser.
2. Click the **Authorize** button (padlock icon near the top right).
3. In the `bearerAuth (http, Bearer)` field, enter:

   ```
   Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

   Replace the token with your actual `accessToken` from `/api/auth/sign-in`.
4. Click **Authorize**, then **Close**.
5. Protected endpoints will now include the `Authorization` header automatically when you click **Try it out**.

> **Tip:** Public endpoints such as `/api/auth/**` do not require authorization even if the padlock is shown.
