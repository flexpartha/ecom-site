# Angular Frontend Integration Reference Manual
### Technical Documentation & Architectural Specifications for `ecommerce-api`

---

## 1. Documentation Purpose & Scope

This documentation suite serves as the **single source of truth** for engineering teams developing a modern, standalone Angular frontend for the Java Spring Boot `ecommerce-api` backend.

All specifications, contracts, and diagrams herein were produced by analyzing the actual Java source code (`com.ecom.api`), Spring configurations, database schema (`db/schema.sql`), and test suites. Nothing has been assumed based on class names alone.

---

## 2. Recommended Reading Order

For engineers, architects, and designers onboarding to the Angular implementation, follow this reading sequence:

```
[1. Project & System Foundations]
   ├── README.md (This File)
   ├── PROJECT_OVERVIEW.md (Tech stack, architecture, layers, Mermaid diagram)
   └── DATABASE_SCHEMA.md (MySQL tables, fields, constraints, enums, ERD)

[2. API Contracts & Technical Flows]
   ├── API_REFERENCE.md (All 25 REST endpoints, parameters, JSON samples)
   ├── AUTHENTICATION_AND_AUTHORIZATION.md (Security reality, customer identity, target JWT)
   └── BUSINESS_WORKFLOWS.md (Sequence diagrams, stock deduction, order state machine)

[3. Angular Client Engineering]
   ├── API_INTEGRATION_GUIDE.md (CORS setup, proxy.conf.json, HttpClient services, errors)
   ├── ANGULAR_DATA_MODELS.md (TypeScript DTOs, interfaces, and enums)
   └── NGRX_ARCHITECTURE_RECOMMENDATION.md (Store structure, actions, reducers, effects, entity adapters)

[4. UX, UI & Roadmap]
   ├── FRONTEND_FEATURE_MAPPING.md (Screen-to-API matrix, form validations, loading states)
   ├── UI_UX_REQUIREMENTS.md (Design system, colors, typography, breakpoints, layouts)
   ├── IMPLEMENTATION_GAP_ANALYSIS.md (Backend defects, missing APIs, security priorities)
   └── ANGULAR_DEVELOPMENT_ROADMAP.md (10-Phase implementation plan with acceptance criteria)
```

---

## 3. Master Document Directory

| Document | Purpose & Key Contents |
| :--- | :--- |
| **[`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md)** | Technical stack (Java 17, Spring Boot 4.1.2, MySQL 8.x), layered architecture, layer flow diagram, component responsibilities. |
| **[`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md)** | Complete MySQL entity and table definitions, data types, constraints, default values, enums (`OrderStatus`, `PaymentMethod`, `PaymentStatus`), and Mermaid ERD. |
| **[`API_REFERENCE.md`](API_REFERENCE.md)** | **Exhaustive documentation for all 25 REST endpoints** across Categories, Products, Customers, Orders, and Payments. Includes realistic JSON payloads, error formats, and validation rules. |
| **[`AUTHENTICATION_AND_AUTHORIZATION.md`](AUTHENTICATION_AND_AUTHORIZATION.md)** | Reality check on lack of Spring Security, interim client-side identity management, target JWT Bearer token architecture, and Angular HttpInterceptor setup. |
| **[`BUSINESS_WORKFLOWS.md`](BUSINESS_WORKFLOWS.md)** | End-to-end sequences with Mermaid diagrams for catalog discovery, customer registration, checkout, stock deduction, order state transitions, and cancellation compensation. |
| **[`API_INTEGRATION_GUIDE.md`](API_INTEGRATION_GUIDE.md)** | Practical Angular developer guide: CORS resolution (`proxy.conf.json`), naming conventions, currency & date formatting, global error interceptor, and injectable services. |
| **[`ANGULAR_DATA_MODELS.md`](ANGULAR_DATA_MODELS.md)** | Copy-pasteable TypeScript interfaces and enums matching backend DTOs, plus client-side models for Cart, Wishlist, and Filter criteria. |
| **[`FRONTEND_FEATURE_MAPPING.md`](FRONTEND_FEATURE_MAPPING.md)** | Mapping of every storefront and admin screen to required APIs, NgRx state, form validation rules, and loading/empty/error states. |
| **[`NGRX_ARCHITECTURE_RECOMMENDATION.md`](NGRX_ARCHITECTURE_RECOMMENDATION.md)** | Scalable NgRx store architecture using `@ngrx/entity`, local storage persistence effects, actions, reducers, and high-performance memoized selectors. |
| **[`UI_UX_REQUIREMENTS.md`](UI_UX_REQUIREMENTS.md)** | Modern, luxury e-commerce design system: color palette, typography scale, spacing, elevation, responsive breakpoints, and component UI blueprints. |
| **[`IMPLEMENTATION_GAP_ANALYSIS.md`](IMPLEMENTATION_GAP_ANALYSIS.md)** | Comprehensive audit of backend gaps: missing security, absence of pagination, lack of fulltext search, missing cart persistence, and CORS issues. |
| **[`ANGULAR_DEVELOPMENT_ROADMAP.md`](ANGULAR_DEVELOPMENT_ROADMAP.md)** | Step-by-step 10-phase engineering roadmap with clear milestones, component lists, dependencies, and verifiable acceptance criteria. |

---

## 4. Complete API Inventory Checklist (25 Endpoints)

| # | HTTP Method | Endpoint URL | Controller & Method | Implementation Status |
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

## 5. Functional Scope Segregation

To ensure clear engineering boundaries, functionality is segregated into three tiers:

### Tier 1: Existing & Ready Backend Functionality
- Full CRUD on Categories (`/api/v1/categories`).
- Full CRUD on Products (`/api/v1/products`) with SKU uniqueness and active status toggle.
- Category filtering (`?categoryId=1`) and active status filtering (`?active=true`).
- Customer directory CRUD (`/api/v1/customers`) with unique email validation.
- Order creation (`POST /api/v1/orders`) with atomic stock reduction and invariant validation.
- Order status state machine (`PENDING` -> `CONFIRMED` -> `PROCESSING` -> `SHIPPED` -> `DELIVERED`).
- Order cancellation with automatic product stock restoration and payment refunding.
- Order lookup by ID or public tracking code (`ORD-XXXXXXXX`).
- Payment record lookup and manual status advancement (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`).

### Tier 2: Partially Implemented / Client-Compensated Functionality
- **Product Search**: Backend lacks text search; Angular executes client-side search across loaded products.
- **Catalog Pagination**: Backend returns unpaged lists; Angular executes client-side pagination via `MatPaginator`.
- **Customer Identity**: Backend lacks passwords; Angular manages customer identity using email lookups.
- **Payment Processing**: Backend simulates payment states; Angular provides a simulated payment step.

### Tier 3: Missing Backend Functionality (Recommended Future Enhancements)
- **Spring Security & JWT Authentication**: User accounts, password hashes, and protected routes.
- **Server-Side Shopping Cart & Wishlist**: Syncing cart items across customer sessions.
- **Structured Address Book**: Normalized address entity with multiple shipping/billing addresses.
- **Product Images & Media Storage**: Image URLs or Cloud storage integration (S3/Cloudinary).
- **Coupons & Promotional Discounts**: Promo code engine.
- **Product Reviews & Star Ratings**: Social proof and feedback mechanisms.
- **CORS Configuration**: Native `@CrossOrigin` configuration in Spring Boot.
