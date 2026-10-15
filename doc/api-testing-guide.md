# API Testing Guide

## Environment variables

This project loads variables from a `.env` file in the project root using Spring Boot's native config import.

1. Copy `.env.example` to `.env`:

   ```bash
   cp .env.example .env
   ```

2. Fill in your real database credentials and JWT secret.
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

All other endpoints, including the customer order APIs, require:

```http
Authorization: Bearer <access_token>
```

The token type must be `ACCESS`. Refresh tokens will be rejected.

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

## Customer Order APIs

All order endpoints are under `/api/v1/orders` and require the Bearer token of the logged-in customer.

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
| `403 Forbidden` | Missing `Authorization` header, wrong token format, expired token, or using a refresh token instead of access token. |
| `400 Bad Request` | Validation failed (e.g., empty items, quantity < 1). |
| `404 Not Found` | Order does not belong to the customer or does not exist. |
| `409` / runtime error | Insufficient stock, order already shipped/cancelled, etc. |

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
