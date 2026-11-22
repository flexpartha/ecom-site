# Complete API Reference: E-Commerce REST API (`v1`)

## 1. Global API Standards & Conventions

All endpoints adhere to the following standards:
- **Base URL**: `http://localhost:8080/api/v1` (Production: `https://api.ecommerce.example.com/api/v1`)
- **Protocol**: HTTP/1.1 or HTTP/2 over HTTPS
- **Content-Type**: `application/json`
- **Accept**: `application/json`
- **Authentication**: **JWT Bearer Token** (Required for protected endpoints — see Auth section). Public endpoints allow anonymous access.
- **Authorization Header**: `Authorization: Bearer <access_token>`
- **CORS**: Enabled for `http://localhost:4200` with credentials
- **Error Response Envelope**: All exceptions return standard JSON:
  ```json
  {
    "timestamp": "2026-11-21T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/v1/resource",
    "errors": {
      "fieldName": "Constraint error message"
    }
  }
  ```

### 1.1 Protected vs Public Endpoints

| Access Level | Endpoints |
| :--- | :--- |
| **Public (Anonymous)** | `GET /api/v1/categories/**`, `GET /api/v1/products/**`, `GET/POST/PUT/DELETE /api/v1/customers/**`, `POST /api/v1/login`, `POST /api/v1/signup`, `POST /api/v1/auth/login`, `POST /api/v1/auth/signup` |
| **Authenticated (JWT Required)** | `POST /api/v1/orders`, `GET /api/v1/orders/**`, `PUT /api/v1/orders/**`, `GET/PUT /api/v1/payments/**`, `POST/PUT/DELETE /api/v1/categories/**` (write), `POST/PUT/DELETE /api/v1/products/**` (write) |

---

## 2. API Inventory Checklist

| # | HTTP Method | Endpoint URL | Controller & Method | Status |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `GET` | `/api/v1/categories` | `CategoryController.getAllCategories()` | **Implemented** |
| 2 | `GET` | `/api/v1/categories/{id}` | `CategoryController.getCategoryById(id)` | **Implemented** |
| 3 | `GET` | `/api/v1/categories/slug/{slug}` | `CategoryController.getCategoryBySlug(slug)` | **Implemented** |
| 4 | `POST` | `/api/v1/categories` | `CategoryController.createCategory(request)` | **Implemented** |
| 5 | `PUT` | `/api/v1/categories/{id}` | `CategoryController.updateCategory(id, request)` | **Implemented** |
| 6 | `DELETE` | `/api/v1/categories/{id}` | `CategoryController.deleteCategory(id)` | **Implemented** |
| 7 | `GET` | `/api/v1/products` | `ProductController.getAllProducts(categoryId, active)` | **Implemented** |
| 8 | `GET` | `/api/v1/products/{id}` | `ProductController.getProductById(id)` | **Implemented** |
| 9 | `GET` | `/api/v1/products/sku/{sku}` | `ProductController.getProductBySku(sku)` | **Implemented** |
| 10 | `POST` | `/api/v1/products` | `ProductController.createProduct(request)` | **Implemented** |
| 11 | `PUT` | `/api/v1/products/{id}` | `ProductController.updateProduct(id, request)` | **Implemented** |
| 12 | `DELETE` | `/api/v1/products/{id}` | `ProductController.deleteProduct(id)` | **Implemented** |
| 13 | `GET` | `/api/v1/customers` | `CustomerController.getAllCustomers()` | **Implemented** |
| 14 | `GET` | `/api/v1/customers/{id}` | `CustomerController.getCustomerById(id)` | **Implemented** |
| 15 | `POST` | `/api/v1/customers` | `CustomerController.createCustomer(request)` | **Implemented** |
| 16 | `PUT` | `/api/v1/customers/{id}` | `CustomerController.updateCustomer(id, request)` | **Implemented** |
| 17 | `DELETE` | `/api/v1/customers/{id}` | `CustomerController.deleteCustomer(id)` | **Implemented** |
| 18 | `POST` | `/api/v1/orders` | `OrderController.createOrder(request)` | **Implemented** |
| 19 | `GET` | `/api/v1/orders` | `OrderController.getAllOrders(customerId, status)` | **Implemented** |
| 20 | `GET` | `/api/v1/orders/{id}` | `OrderController.getOrderById(id)` | **Implemented** |
| 21 | `GET` | `/api/v1/orders/number/{orderNumber}`| `OrderController.getOrderByOrderNumber(orderNumber)` | **Implemented** |
| 22 | `PUT` | `/api/v1/orders/{id}/status` | `OrderController.updateOrderStatus(id, request)` | **Implemented** |
| 23 | `PUT` | `/api/v1/orders/{id}/cancel` | `OrderController.cancelOrder(id)` | **Implemented** |
| 24 | `GET` | `/api/v1/payments/order/{orderId}` | `PaymentController.getPaymentByOrderId(orderId)` | **Implemented** |
| 25 | `PUT` | `/api/v1/payments/order/{orderId}/status` | `PaymentController.updatePaymentStatus(orderId, request)` | **Implemented** |

---

## 3. Module Status Overview

| Module Name | Status in Backend | Notes / Angular Action Required |
| :--- | :---: | :--- |
| **Product Categories** | **IMPLEMENTED** | Full CRUD, slug & ID queries. Write ops require JWT. |
| **Product Management & Catalog** | **IMPLEMENTED** | Full CRUD, SKU & ID queries, basic filtering. Write ops require JWT. |
| **Customer Directory** | **IMPLEMENTED** | Full CRUD, email uniqueness. Public access. |
| **Orders & Checkout Placement** | **IMPLEMENTED** | Creation, status transitions, stock reduction, cancellation. **Requires JWT**. |
| **Payment Tracking** | **IMPLEMENTED** | Read & update status per order. **Requires JWT**. |
| **Authentication & Registration** | **IMPLEMENTED** | `POST /api/v1/login`, `POST /api/v1/signup` (aliases `/auth/login`, `/auth/signup`). JWT Bearer tokens. |
| **User Profile & Password** | **PARTIAL** | Login/signup implemented. No profile update, password change, or refresh token endpoint yet. |
| **Shopping Cart** | **MISSING** | Must be implemented 100% in Angular client (LocalStorage / NgRx) |
| **Wishlist** | **MISSING** | Must be client-side state in Angular |
| **Address Management** | **MISSING** | No dedicated address book; raw string stored in Customer / Order |
| **Product Search (Fulltext)** | **MISSING** | No search endpoint; Angular must filter client-side or use category filters |
| **Coupons & Discounts** | **MISSING** | No discount or coupon calculation APIs |
| **Reviews & Ratings** | **MISSING** | No feedback or rating APIs |
| **Shipping & Carrier Tracking** | **MISSING** | Status limited to OrderStatus (`SHIPPED`, `DELIVERED`) |

---

## 4. Module: Product Categories

### 4.1 Get All Categories
- **Description**: Retrieves a complete list of all product categories.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/categories`
- **Controller Method**: `CategoryController.getAllCategories()`
- **Auth / Role**: None (Public)
- **Path / Query Parameters**: None
- **Pagination & Sorting**: **None** (Returns all categories as a plain JSON array).
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "name": "Electronics",
      "slug": "electronics",
      "description": "Gadgets, accessories and devices",
      "createdAt": "2026-11-21T09:00:00",
      "updatedAt": "2026-11-21T09:00:00"
    },
    {
      "id": 2,
      "name": "Books",
      "slug": "books",
      "description": "Printed and reference books",
      "createdAt": "2026-11-21T09:00:00",
      "updatedAt": "2026-11-21T09:00:00"
    }
  ]
  ```

---

### 4.2 Get Category by ID
- **Description**: Retrieves details of a specific category using its database ID.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/categories/{id}`
- **Controller Method**: `CategoryController.getCategoryById(@PathVariable Long id)`
- **Auth / Role**: None (Public)
- **Path Parameters**:
  - `id` (`Long`, required): Primary key of the category.
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics",
    "description": "Gadgets, accessories and devices",
    "createdAt": "2026-11-21T09:00:00",
    "updatedAt": "2026-11-21T09:00:00"
  }
  ```
- **Error Responses**:
  - `404 Not Found`:
    ```json
    {
      "timestamp": "2026-11-21T09:15:00",
      "status": 404,
      "error": "Not Found",
      "message": "Category not found with id: 99",
      "path": "/api/v1/categories/99"
    }
    ```

---

### 4.3 Get Category by Slug
- **Description**: Retrieves category details using its unique SEO slug. Ideal for Angular routing (`/category/:slug`).
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/categories/slug/{slug}`
- **Controller Method**: `CategoryController.getCategoryBySlug(@PathVariable String slug)`
- **Auth / Role**: None (Public)
- **Path Parameters**:
  - `slug` (`String`, required): Unique URL-friendly slug.
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics",
    "description": "Gadgets, accessories and devices",
    "createdAt": "2026-11-21T09:00:00",
    "updatedAt": "2026-11-21T09:00:00"
  }
  ```
- **Error Responses**:
  - `404 Not Found`:
    ```json
    {
      "timestamp": "2026-11-21T09:15:00",
      "status": 404,
      "error": "Not Found",
      "message": "Category not found with slug: invalid-slug",
      "path": "/api/v1/categories/slug/invalid-slug"
    }
    ```

---

### 4.4 Create Category
- **Description**: Adds a new category to the database.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/categories`
- **Controller Method**: `CategoryController.createCategory(@Valid @RequestBody CategoryRequest request)`
- **Auth / Role**: None (Administrative operation, currently unprotected)
- **Validation Rules**:
  - `name`: `@NotBlank`, max 100 characters. Must be unique.
  - `slug`: `@NotBlank`, max 100 characters. Must be unique.
  - `description`: Optional text.
- **Request Body**:
  ```json
  {
    "name": "Gaming",
    "slug": "gaming",
    "description": "Video game consoles, games, and accessories"
  }
  ```
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 5,
    "name": "Gaming",
    "slug": "gaming",
    "description": "Video game consoles, games, and accessories",
    "createdAt": "2026-11-21T10:00:00",
    "updatedAt": "2026-11-21T10:00:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request` (Validation Failure):
    ```json
    {
      "timestamp": "2026-11-21T10:01:00",
      "status": 400,
      "error": "Bad Request",
      "message": "Validation failed",
      "path": "/api/v1/categories",
      "errors": {
        "name": "Category name is required",
        "slug": "Category slug is required"
      }
    }
    ```
  - `409 Conflict` (Duplicate name or slug):
    ```json
    {
      "timestamp": "2026-11-21T10:02:00",
      "status": 409,
      "error": "Conflict",
      "message": "Category already exists with name: Electronics",
      "path": "/api/v1/categories"
    }
    ```

---

### 4.5 Update Category
- **Description**: Updates an existing category by ID.
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/categories/{id}`
- **Controller Method**: `CategoryController.updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request)`
- **Path Parameters**: `id` (`Long`, required)
- **Request Body**:
  ```json
  {
    "name": "Consumer Electronics",
    "slug": "consumer-electronics",
    "description": "Updated description for gadgets and devices"
  }
  ```
- **Success Response (`200 OK`)**: Updated `CategoryResponse` object.
- **Error Responses**: `400 Bad Request`, `404 Not Found`, `409 Conflict` (if new name/slug duplicates another category).

---

### 4.6 Delete Category
- **Description**: Removes a category record by ID.
- **HTTP Method**: `DELETE`
- **Endpoint**: `/api/v1/categories/{id}`
- **Controller Method**: `CategoryController.deleteCategory(@PathVariable Long id)`
- **Path Parameters**: `id` (`Long`, required)
- **Success Response (`204 No Content`)**: Empty response body.
- **Error Responses**:
  - `404 Not Found`: If category ID does not exist.
  - `500 Internal Server Error`: If foreign key constraint is violated (e.g. products reference this category).

---

## 5. Module: Products

### 5.1 Get All Products (Catalog with Filters)
- **Description**: Retrieves products. Supports optional query parameters for filtering by category and active status.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/products`
- **Controller Method**: `ProductController.getAllProducts(@RequestParam(required = false) Long categoryId, @RequestParam(required = false) Boolean active)`
- **Query Parameters**:
  - `categoryId` (`Long`, optional): Filter products belonging to a specific category.
  - `active` (`Boolean`, optional): Pass `true` for active products, `false` for inactive.
- **Pagination & Sorting**: **None** in backend. Returns a full `List<ProductResponse>`.
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "name": "Wireless Mouse",
      "description": "Ergonomic 2.4GHz wireless mouse",
      "price": 24.99,
      "sku": "ELEC-MOU-001",
      "stockQuantity": 150,
      "active": true,
      "categoryId": 1,
      "categoryName": "Electronics",
      "createdAt": "2026-11-21T09:00:00",
      "updatedAt": "2026-11-21T09:00:00"
    },
    {
      "id": 2,
      "name": "Mechanical Keyboard",
      "description": "Tactile mechanical keyboard, RGB backlit",
      "price": 79.99,
      "sku": "ELEC-KEY-002",
      "stockQuantity": 80,
      "active": true,
      "categoryId": 1,
      "categoryName": "Electronics",
      "createdAt": "2026-11-21T09:00:00",
      "updatedAt": "2026-11-21T09:00:00"
    }
  ]
  ```

---

### 5.2 Get Product by ID
- **Description**: Retrieves a single product by primary key ID.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/products/{id}`
- **Controller Method**: `ProductController.getProductById(@PathVariable Long id)`
- **Path Parameters**: `id` (`Long`, required)
- **Success Response (`200 OK`)**: Single `ProductResponse` JSON object.
- **Error Responses**:
  - `404 Not Found`: `"Product not found with id: {id}"`

---

### 5.3 Get Product by SKU
- **Description**: Retrieves product details using unique Stock Keeping Unit (SKU).
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/products/sku/{sku}`
- **Controller Method**: `ProductController.getProductBySku(@PathVariable String sku)`
- **Path Parameters**: `sku` (`String`, required)
- **Success Response (`200 OK`)**: Single `ProductResponse` JSON object.
- **Error Responses**:
  - `404 Not Found`: `"Product not found with SKU: {sku}"`

---

### 5.4 Create Product
- **Description**: Creates a new product catalog entry.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/products`
- **Controller Method**: `ProductController.createProduct(@Valid @RequestBody ProductRequest request)`
- **Validation Rules**:
  - `name`: `@NotBlank`, max 200 chars.
  - `price`: `@NotNull`, `@DecimalMin(value = "0.00", inclusive = false)` (must be > 0.00).
  - `sku`: `@NotBlank`, max 100 chars, must be unique.
  - `stockQuantity`: `@NotNull`, `@Min(0)`.
  - `active`: Boolean, defaults to `true` if omitted.
  - `categoryId`: `@NotNull`, must reference an existing category.
- **Request Body**:
  ```json
  {
    "name": "Ultra HD 4K Monitor",
    "description": "27-inch 4K UHD IPS display with HDR400",
    "price": 349.99,
    "sku": "ELEC-MON-005",
    "stockQuantity": 25,
    "active": true,
    "categoryId": 1
  }
  ```
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 11,
    "name": "Ultra HD 4K Monitor",
    "description": "27-inch 4K UHD IPS display with HDR400",
    "price": 349.99,
    "sku": "ELEC-MON-005",
    "stockQuantity": 25,
    "active": true,
    "categoryId": 1,
    "categoryName": "Electronics",
    "createdAt": "2026-11-21T10:30:00",
    "updatedAt": "2026-11-21T10:30:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Validation failure.
  - `404 Not Found`: `"Category not found with id: {categoryId}"`
  - `409 Conflict`: `"Product already exists with SKU: {sku}"`

---

### 5.5 Update Product
- **Description**: Updates an existing product's details and inventory level.
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/products/{id}`
- **Controller Method**: `ProductController.updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request)`
- **Success Response (`200 OK`)**: Updated `ProductResponse`.
- **Error Responses**: `400 Bad Request`, `404 Not Found` (if product or category missing), `409 Conflict` (if new SKU is taken).

---

### 5.6 Delete Product
- **Description**: Deletes a product from the database.
- **HTTP Method**: `DELETE`
- **Endpoint**: `/api/v1/products/{id}`
- **Controller Method**: `ProductController.deleteProduct(@PathVariable Long id)`
- **Success Response (`204 No Content`)**: Empty response body.
- **Error Responses**: `404 Not Found` (if product id not found).

---

## 6. Module: Customers

### 6.1 Get All Customers
- **Description**: Retrieves all customer profiles.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/customers`
- **Controller Method**: `CustomerController.getAllCustomers()`
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+1-202-555-0101",
      "address": "123 Maple Street, Springfield",
      "createdAt": "2026-11-21T09:00:00"
    }
  ]
  ```

---

### 6.2 Get Customer by ID
- **Description**: Retrieves customer details by ID.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/customers/{id}`
- **Controller Method**: `CustomerController.getCustomerById(@PathVariable Long id)`
- **Success Response (`200 OK`)**: Single `CustomerResponse` JSON.
- **Error Responses**: `404 Not Found`: `"Customer not found with id: {id}"`

---

### 6.3 Create Customer (Client-Side Guest / Registration Equivalent)
- **Description**: Registers a customer record in the database.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/customers`
- **Controller Method**: `CustomerController.createCustomer(@Valid @RequestBody CustomerRequest request)`
- **Validation Rules**:
  - `firstName`: `@NotBlank`, max 100 chars.
  - `lastName`: `@NotBlank`, max 100 chars.
  - `email`: `@NotBlank`, `@Email`, max 255 chars, unique.
  - `phone`: Optional, max 20 chars.
  - `address`: Optional text.
- **Request Body**:
  ```json
  {
    "firstName": "Alex",
    "lastName": "Taylor",
    "email": "alex.taylor@example.com",
    "phone": "+1-555-0199",
    "address": "456 Innovation Way, Tech City"
  }
  ```
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 4,
    "firstName": "Alex",
    "lastName": "Taylor",
    "email": "alex.taylor@example.com",
    "phone": "+1-555-0199",
    "address": "456 Innovation Way, Tech City",
    "createdAt": "2026-11-21T11:00:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Field validation errors.
  - `409 Conflict`: `"Customer already exists with email: alex.taylor@example.com"`

---

### 6.4 Update Customer
- **Description**: Updates an existing customer profile.
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/customers/{id}`
- **Controller Method**: `CustomerController.updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request)`
- **Success Response (`200 OK`)**: Updated `CustomerResponse`.
- **Error Responses**: `400 Bad Request`, `404 Not Found`, `409 Conflict` (if email already registered to another customer).

---

### 6.5 Delete Customer
- **Description**: Deletes a customer by ID.
- **HTTP Method**: `DELETE`
- **Endpoint**: `/api/v1/customers/{id}`
- **Controller Method**: `CustomerController.deleteCustomer(@PathVariable Long id)`
- **Success Response (`204 No Content`)**: Empty response body.
- **Error Responses**: `404 Not Found`.

---

## 7. Module: Orders

### 7.1 Create Order (Checkout Placement)
- **Description**: Submits a customer checkout request. Executes stock validation, reduces product inventory, calculates totals, creates order items, and creates a pending payment record.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/orders`
- **Controller Method**: `OrderController.createOrder(@Valid @RequestBody CreateOrderRequest request)`
- **Validation Rules**:
  - `customerId`: `@NotNull` (must reference existing Customer).
  - `items`: `@NotEmpty`, contains at least 1 `@Valid` `OrderItemRequest`.
  - `items[].productId`: `@NotNull`.
  - `items[].quantity`: `@NotNull`, `@Min(1)`.
  - `shippingAddress`: `@NotBlank`.
  - `notes`: Optional string.
  - `paymentMethod`: `@NotNull` (Enum: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `WALLET`, `CASH_ON_DELIVERY`).
- **Request Body**:
  ```json
  {
    "customerId": 1,
    "shippingAddress": "123 Maple Street, Springfield, IL 62701",
    "notes": "Please leave at front door",
    "paymentMethod": "CREDIT_CARD",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 3,
        "quantity": 1
      }
    ]
  }
  ```
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 1,
    "orderNumber": "ORD-A1B2C3D4",
    "customerId": 1,
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "status": "PENDING",
    "totalAmount": 89.97,
    "shippingAddress": "123 Maple Street, Springfield, IL 62701",
    "notes": "Please leave at front door",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Wireless Mouse",
        "productSku": "ELEC-MOU-001",
        "quantity": 2,
        "unitPrice": 24.99,
        "subtotal": 49.98
      },
      {
        "id": 2,
        "productId": 3,
        "productName": "USB-C Hub",
        "productSku": "ELEC-HUB-003",
        "quantity": 1,
        "unitPrice": 39.99,
        "subtotal": 39.99
      }
    ],
    "payment": {
      "id": 1,
      "orderId": 1,
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PENDING",
      "amount": 89.97,
      "createdAt": "2026-11-21T11:15:00"
    },
    "createdAt": "2026-11-21T11:15:00",
    "updatedAt": "2026-11-21T11:15:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Validation failure (empty items list, missing shipping address, etc.).
  - `404 Not Found`: Customer or Product ID does not exist.
  - `422 Unprocessable Entity` (Business rule violation):
    - When product is inactive:
      ```json
      {
        "timestamp": "2026-11-21T11:16:00",
        "status": 422,
        "error": "Unprocessable Entity",
        "message": "Product is not active: Wireless Mouse",
        "path": "/api/v1/orders"
      }
      ```
    - When stock is insufficient:
      ```json
      {
        "timestamp": "2026-11-21T11:16:00",
        "status": 422,
        "error": "Unprocessable Entity",
        "message": "Insufficient stock for product: Wireless Mouse. Available: 3, requested: 5",
        "path": "/api/v1/orders"
      }
      ```

---

### 7.2 Get All Orders (Order History)
- **Description**: Retrieves a list of orders. Supports filtering by customer ID and order status.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/orders`
- **Controller Method**: `OrderController.getAllOrders(@RequestParam(required = false) Long customerId, @RequestParam(required = false) OrderStatus status)`
- **Query Parameters**:
  - `customerId` (`Long`, optional): Get all orders for this customer (used for Customer Order History).
  - `status` (`OrderStatus`, optional): Filter by status (`PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUNDED`).
- **Success Response (`200 OK`)**: List of `OrderResponse` objects.

---

### 7.3 Get Order by ID
- **Description**: Retrieves order details by database ID.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/orders/{id}`
- **Controller Method**: `OrderController.getOrderById(@PathVariable Long id)`
- **Success Response (`200 OK`)**: Single `OrderResponse` object.
- **Error Responses**: `404 Not Found`: `"Order not found with id: {id}"`

---

### 7.4 Get Order by Order Number
- **Description**: Retrieves order details by public tracking reference (e.g. `ORD-A1B2C3D4`).
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/orders/number/{orderNumber}`
- **Controller Method**: `OrderController.getOrderByOrderNumber(@PathVariable String orderNumber)`
- **Success Response (`200 OK`)**: Single `OrderResponse` object.
- **Error Responses**: `404 Not Found`: `"Order not found with order number: {orderNumber}"`

---

### 7.5 Update Order Status
- **Description**: Advances the order state machine.
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/orders/{id}/status`
- **Controller Method**: `OrderController.updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request)`
- **Allowed Transitions**:
  - `PENDING` -> `CONFIRMED`, `CANCELLED`
  - `CONFIRMED` -> `PROCESSING`, `CANCELLED`
  - `PROCESSING` -> `SHIPPED`, `CANCELLED`
  - `SHIPPED` -> `DELIVERED`
  - `DELIVERED`, `CANCELLED`, `REFUNDED` -> None (Terminal)
- **Request Body**:
  ```json
  {
    "status": "CONFIRMED"
  }
  ```
- **Success Response (`200 OK`)**: Updated `OrderResponse`.
- **Error Responses**:
  - `400 Bad Request`: Null or unrecognized status.
  - `404 Not Found`: Order ID missing.
  - `422 Unprocessable Entity`: Invalid status transition (e.g. `PENDING` directly to `SHIPPED` or transition from `DELIVERED`).

---

### 7.6 Cancel Order
- **Description**: Cancels an order. Restores stock quantities for all items. If payment status was `COMPLETED`, updates payment to `REFUNDED`.
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/orders/{id}/cancel`
- **Controller Method**: `OrderController.cancelOrder(@PathVariable Long id)`
- **Path Parameters**: `id` (`Long`, required)
- **Success Response (`200 OK`)**: Updated `OrderResponse` with `status: "CANCELLED"`.
- **Error Responses**:
  - `404 Not Found`: Order ID missing.
  - `422 Unprocessable Entity`: If order is already in a terminal state (`DELIVERED`, `CANCELLED`).

---

## 8. Module: Payments

### 8.1 Get Payment by Order ID
- **Description**: Retrieves payment transaction information associated with an order.
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/payments/order/{orderId}`
- **Controller Method**: `PaymentController.getPaymentByOrderId(@PathVariable Long orderId)`
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "orderId": 1,
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "amount": 89.97,
    "createdAt": "2026-11-21T11:15:00"
  }
  ```
- **Error Responses**:
  - `404 Not Found`: `"Payment not found for order id: {orderId}"`

---

### 8.2 Update Payment Status
- **Description**: Updates payment transaction status (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`).
- **HTTP Method**: `PUT`
- **Endpoint**: `/api/v1/payments/order/{orderId}/status`
- **Controller Method**: `PaymentController.updatePaymentStatus(@PathVariable Long orderId, @Valid @RequestBody UpdatePaymentStatusRequest request)`
- **Validation Rules**:
  - `paymentStatus`: `@NotNull` (Enum: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`).
- **Request Body**:
  ```json
  {
    "paymentStatus": "COMPLETED"
  }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "orderId": 1,
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "COMPLETED",
    "amount": 89.97,
    "createdAt": "2026-11-21T11:15:00"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Missing or invalid `paymentStatus`.
  - `404 Not Found`: No payment record found for order.

---

## 9. Module: Authentication & User Management

### 9.1 User Login
- **Description**: Authenticates user credentials and returns a JWT access token.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/login` (also `/api/v1/auth/login`)
- **Controller Method**: `AuthController.login(@Valid @RequestBody LoginRequest request)`
- **Auth / Role**: Public (Anonymous)
- **Request Body**:
  ```json
  {
    "username": "partha.chakra2",
    "password": "admin123"
  }
  ```
- **Validation Rules**:
  - `username`: `@NotBlank`
  - `password`: `@NotBlank`
- **Success Response (`200 OK`)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "firstName": "Partha",
    "lastName": "Chakraborty"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Missing username or password
  - `401 Unauthorized`: Invalid credentials

---

### 9.2 User Registration (Signup)
- **Description**: Registers a new user account with hashed password.
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/signup` (also `/api/v1/auth/signup`)
- **Controller Method**: `AuthController.signup(@Valid @RequestBody SignupRequest request)`
- **Auth / Role**: Public (Anonymous)
- **Request Body**:
  ```json
  {
    "firstName": "Partha",
    "lastName": "Chakraborty",
    "username": "partha.chakra2",
    "password": "admin123"
  }
  ```
- **Validation Rules**:
  - `firstName`: `@NotBlank`, max 100 chars
  - `lastName`: `@NotBlank`, max 100 chars
  - `username`: `@NotBlank`, max 100 chars, unique
  - `password`: `@NotBlank`, min 6 chars
- **Success Response (`200 OK`)**:
  ```json
  {
    "status": 200,
    "message": "Registration successful"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Validation failures
  - `409 Conflict`: Username already exists
