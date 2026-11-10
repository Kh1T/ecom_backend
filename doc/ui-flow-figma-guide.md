# UI/UX Flow & Figma Screen Guide

This document maps the backend APIs to frontend screens and user flows to help design Figma prototypes.

---

## User Roles

| Role | Description |
|------|-------------|
| **Guest** | Not logged in. Can browse products only. |
| **Customer** | Logged-in shopper. Can browse, order, and manage own orders. |
| **Admin** | Full access: catalog management, brands, categories, products, orders. |
| **Catalog Manager** | Can manage catalog (products, categories, brands) but not orders/users. |

---

## 1. Authentication Flow

### APIs

- `POST /api/auth/sign-up`
- `POST /api/auth/sign-in`
- `POST /api/auth/refresh`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/me`

### Screens

#### 1.1 Sign Up
- Email input
- Password input (min 6 chars)
- First name / Last name inputs
- Submit button
- Link to Sign In

#### 1.2 Sign In
- Email input
- Password input
- Submit button
- Link to Sign Up
- Link to Forgot Password

#### 1.3 Forgot Password
- Email input
- Submit button
- Shows reset token (dev/test only; in production this would be emailed)

#### 1.4 Reset Password
- Token input
- New password input
- Submit button

### Flow

```
Sign In ──→ Browse Products
   │
   └──→ Sign Up ──→ Sign In
   │
   └──→ Forgot Password ──→ Reset Password ──→ Sign In
```

---

## 2. Customer Shopping Flow

### APIs

- `GET /api/v1/products?page=&size=` — product listing
- `GET /api/v1/products/{id}` — product details
- `GET /api/v1/products/search?q=` — search products

### Screens

#### 2.1 Product Listing (Home / Shop)
- Header with search bar
- Category filter (optional, from product category data)
- Product grid/card list
- Each card shows: image, name, price, category
- Pagination controls

#### 2.2 Search Results
- Same layout as Product Listing
- Shows query term and result count
- Empty state when no results

#### 2.3 Product Details
- Large product image gallery (from Cloudinary)
- Product name, price, description
- SKU, category, brand
- Stock availability
- Variant selector (size/color/etc.)
- Quantity selector
- Add to Cart / Buy Now button

### Flow

```
Home / Shop
   │
   ├──→ Search Results
   │
   └──→ Product Details ──→ Add to Cart ──→ Checkout
```

---

## 3. Customer Order Flow

### APIs

- `POST /api/v1/orders` — create order from cart items
- `GET /api/v1/orders` — order history
- `GET /api/v1/orders/{orderId}` — order details
- `POST /api/v1/orders/{orderId}/cancel` — cancel order
- `GET /api/v1/orders/{orderId}/tracking` — shipment tracking
- `POST /api/v1/orders/{orderId}/confirm-delivery` — confirm delivery

### Screens

#### 3.1 Cart / Checkout
- List of selected products with quantities
- Total price
- Delivery address (can be simple text for now)
- Place Order button

#### 3.2 Order Success
- Order ID
- Summary of items
- Continue shopping button

#### 3.3 Order History
- List of customer orders
- Each row: order ID, date, status, total amount
- Newest first

#### 3.4 Order Details
- Order ID, status, dates
- Item list with quantities and prices
- Total amount
- Action buttons based on status:
  - `PENDING` → Cancel Order
  - `SHIPPED` / `PENDING` → Confirm Delivery
- Tracking section

#### 3.5 Order Tracking
- Tracking number
- Carrier
- Shipment status
- Estimated delivery date

### Flow

```
Cart ──→ Place Order ──→ Order Success
   │
   └──→ Order History ──→ Order Details
                              │
                              ├──→ Cancel Order
                              ├──→ View Tracking
                              └──→ Confirm Delivery
```

### Order Statuses

| Status | Customer Actions |
|--------|------------------|
| `PENDING` | Cancel, Confirm Delivery |
| `CONFIRMED` | View details, tracking |
| `SHIPPED` | Confirm Delivery, tracking |
| `DELIVERED` | View details |
| `CANCELLED` | View details |

---

## 4. Admin Catalog Management Flow

### APIs

- `POST /api/v1/admin/products`
- `GET /api/v1/admin/products/{id}`
- `PUT /api/v1/admin/products/{id}`
- `PATCH /api/v1/admin/products/{id}/status`
- `DELETE /api/v1/admin/products/{id}`
- `POST /api/v1/admin/products/{id}/images`
- `DELETE /api/v1/admin/products/{id}/images/{imageId}`
- `POST /api/v1/admin/products/{id}/variants`
- `PATCH /api/v1/admin/products/{id}/variants/{variantId}`
- `POST /api/v1/admin/categories`
- `PUT /api/v1/admin/categories/{id}`
- `POST /api/v1/admin/brands`

### Screens

#### 4.1 Admin Dashboard
- Navigation sidebar: Products, Categories, Brands, Orders
- Summary cards (optional)

#### 4.2 Product List (Admin)
- Table/grid of products
- Columns: ID, image, name, price, stock, status, category, brand
- Filters: status (active/archived), category, brand
- Actions: Edit, Archive/Activate, Delete
- Button: Add New Product

#### 4.3 Add / Edit Product
- Basic info: name, description, price, stock, SKU
- Status dropdown: ACTIVE / ARCHIVED
- Category dropdown
- Brand dropdown
- Image upload section
- Variants section
- Save button

#### 4.4 Product Image Upload
- Drag-and-drop or file picker
- Preview uploaded images
- Sort order input
- Delete image button

#### 4.5 Product Variants
- Variant name (e.g., "Red - Large")
- SKU
- Price adjustment
- Stock quantity
- Add / Update / Remove variant

#### 4.6 Categories Management
- List of categories
- Add category button
- Edit category modal/form

#### 4.7 Brands Management
- List of brands
- Add brand button

### Flow

```
Admin Dashboard
   │
   ├──→ Products List ──→ Add Product / Edit Product
   │                          │
   │                          ├──→ Upload Images
   │                          └──→ Manage Variants
   │
   ├──→ Categories ──→ Add / Edit Category
   │
   └──→ Brands ──→ Add Brand
```

---

## 5. Screen Checklist for Figma

### Public Screens

- [ ] Sign In
- [ ] Sign Up
- [ ] Forgot Password
- [ ] Reset Password
- [ ] Home / Product Listing
- [ ] Search Results
- [ ] Product Details

### Customer Screens (requires login)

- [ ] Cart / Checkout
- [ ] Order Success
- [ ] Order History
- [ ] Order Details
- [ ] Order Tracking

### Admin Screens (requires ADMIN or CATALOG_MANAGER)

- [ ] Admin Dashboard
- [ ] Admin Product List
- [ ] Add Product
- [ ] Edit Product
- [ ] Category List
- [ ] Brand List

---

## 6. Notes for Figma Design

1. **Mobile-first or responsive**: Consider both desktop and mobile layouts.
2. **Role-based navigation**: Customer and Admin should see different nav items.
3. **Status badges**: Use color-coded badges for order and product statuses.
4. **Image handling**: Product images come from Cloudinary; use image placeholders.
5. **Empty states**: Design empty states for search results, order history, and lists.
6. **Loading states**: Buttons and screens should show loading indicators during API calls.
7. **Error messages**: Plan spaces for inline validation and API error messages.
