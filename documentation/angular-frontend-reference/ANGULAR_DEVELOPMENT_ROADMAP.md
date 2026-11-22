# Angular Frontend Development Roadmap

## 1. Project Execution Overview

This document outlines a structured, 10-phase engineering roadmap for developing a modern, production-ready Angular frontend for the `ecommerce-api` backend. 

Each phase details specific features, consumed backend APIs, NgRx state management requirements, UI components to construct, dependencies, and rigorous acceptance criteria.

---

## 2. Phase-by-Phase Implementation Roadmap

### Phase 1: Angular Project Setup & Core Architecture
- **Objective**: Establish the foundational Angular workspace, build pipeline, network layer, and core state infrastructure.
- **Features**:
  - Initialize Angular 16+ project with standalone or modular architecture and SCSS styling.
  - Setup Angular CLI proxy (`proxy.conf.json`) targeting `http://localhost:8080` to resolve CORS.
  - Configure Angular Material and utility CSS (Tailwind CSS or Bootstrap utilities).
  - Setup NgRx Store, Effects, Entity, and Store DevTools.
  - Implement `GlobalHttpErrorInterceptor` and base API services.
- **Backend APIs Required**: `GET /actuator/health`
- **Dependencies**: `@angular/material`, `@angular/cdk`, `@ngrx/store`, `@ngrx/effects`, `@ngrx/entity`, `@ngrx/store-devtools`.
- **Acceptance Criteria**:
  - `ng serve` successfully proxies `/api/v1` calls to the local Spring Boot backend.
  - Global error interceptor captures and displays `ErrorResponse` payloads via `MatSnackBar`.
  - NgRx Store DevTools inspects initial state in browser developer tools.

---

### Phase 2: Shared Components & Design System
- **Objective**: Build reusable, accessible UI components embodying the premium e-commerce design system.
- **Features**:
  - Global responsive header with search bar, category navigation, cart badge, and user menu.
  - Footer with customer service links, payment badges, and newsletter signup.
  - Reusable Product Card Component with image aspect ratio box, price formatting, and stock badges.
  - Loading Skeleton Shimmer components for catalog and detail views.
  - Quantity Stepper Component (`-` / `+` with min/max bounds).
  - Price Tag and Currency Pipe formatting.
- **Backend APIs Required**: None.
- **UI Components**: `HeaderComponent`, `FooterComponent`, `ProductCardComponent`, `SkeletonCardComponent`, `QuantityStepperComponent`, `StockBadgeComponent`.
- **Acceptance Criteria**:
  - All shared components are responsive across mobile (375px), tablet (768px), and desktop (1280px).
  - Design tokens (colors, typography, elevation) match the design specification.

---

### Phase 3: Customer Identity & Auth Management
- **Objective**: Implement client-side customer identity management and prepare for JWT authentication.
- **Features**:
  - Customer selection/switch modal (for development and guest testing).
  - Customer registration form.
  - Interim customer session persistence in `localStorage`.
  - `AuthInterceptor` (attaching Bearer token if present).
  - `AuthGuard` protecting checkout and order history routes.
- **Backend APIs Required**:
  - `POST /api/v1/customers` (Create customer)
  - `GET /api/v1/customers` (List customers)
  - `GET /api/v1/customers/{id}` (Get customer)
- **NgRx Requirements**: `auth` slice (`currentCustomer`, `isAuthenticated`, `guestMode`).
- **UI Components**: `CustomerSelectModalComponent`, `RegisterFormComponent`.
- **Acceptance Criteria**:
  - Customer profile creation successfully validates required fields and handles `409 Conflict` (duplicate email).
  - Selected customer context persists across page refreshes.

---

### Phase 4: Product Catalog, Categories & Search
- **Objective**: Deliver a fluid browsing experience across categories and products.
- **Features**:
  - Storefront Homepage with featured categories and active products.
  - Category Showcase page (`/category/:slug`).
  - Product Listing Page (PLP) (`/products`) with multi-facet client-side filtering (category, price slider, in-stock toggle, text search).
  - Product Detail Page (PDP) (`/products/:id`) with SKU display, inventory indicator, and sticky buy box.
- **Backend APIs Required**:
  - `GET /api/v1/categories`
  - `GET /api/v1/categories/slug/{slug}`
  - `GET /api/v1/products` (with `categoryId` and `active` params)
  - `GET /api/v1/products/{id}`
  - `GET /api/v1/products/sku/{sku}`
- **NgRx Requirements**: `categories` and `products` slices using `@ngrx/entity` with filtering selectors.
- **UI Components**: `HomeComponent`, `CategoryPillsComponent`, `ProductListComponent`, `ProductDetailComponent`, `ProductFilterSidebarComponent`.
- **Acceptance Criteria**:
  - Category slug lookup correctly loads associated category products.
  - Real-time client search filters products by title and description within 300ms debounce.
  - Out-of-stock products (`stockQuantity === 0`) disable the "Add to Cart" button.

---

### Phase 5: Shopping Cart & Wishlist
- **Objective**: Deliver high-converting cart and wishlist management.
- **Features**:
  - Slide-over mini-cart drawer triggering on product add.
  - Dedicated Shopping Cart page (`/cart`) with line item editing and quantity adjustment.
  - Subtotal and estimated tax/shipping calculation.
  - Wishlist page (`/wishlist`) with "Move to Cart" action.
  - Automatic synchronization of cart and wishlist to `localStorage`.
- **Backend APIs Required**: None (client-side state).
- **NgRx Requirements**: `cart` and `wishlist` slices with localStorage sync effects.
- **UI Components**: `MiniCartDrawerComponent`, `CartPageComponent`, `WishlistPageComponent`.
- **Acceptance Criteria**:
  - Cart prevents selecting quantities higher than `product.stockQuantity`.
  - Cart persists across browser tab closures and reloads.
  - Header cart badge accurately reflects total item quantity.

---

### Phase 6: Checkout & Address Handling
- **Objective**: Construct a frictionless, multi-step checkout funnel.
- **Features**:
  - Multi-step checkout wizard (`MatStepper`):
    1. Customer Profile verification / creation.
    2. Shipping Address & delivery notes.
    3. Payment Method selection.
    4. Order Review & submission.
  - Form validation with descriptive error messages.
- **Backend APIs Required**:
  - `POST /api/v1/customers`
  - `POST /api/v1/orders`
- **NgRx Requirements**: `checkout` state coordinating cart items and customer profile.
- **UI Components**: `CheckoutWizardComponent`, `AddressFormComponent`, `PaymentMethodSelectComponent`.
- **Acceptance Criteria**:
  - Checkout gracefully blocks submission if cart is empty.
  - Submitting an order invokes `POST /api/v1/orders` and transitions to Confirmation page on `201 Created`.
  - Catches `422 Unprocessable Entity` (insufficient stock) and highlights the problematic line item.

---

### Phase 7: Orders & Payment Simulation
- **Objective**: Complete the transaction lifecycle and order tracking.
- **Features**:
  - Order Confirmation screen (`/orders/:orderNumber/confirmed`) with printable receipt and reference code.
  - Simulated payment completion workflow: updates `PaymentStatus` to `COMPLETED`.
  - Public order tracking page (`/track-order`) searching by `orderNumber`.
- **Backend APIs Required**:
  - `GET /api/v1/orders/number/{orderNumber}`
  - `GET /api/v1/payments/order/{orderId}`
  - `PUT /api/v1/payments/order/{orderId}/status`
- **NgRx Requirements**: `orders` slice tracking `activeOrder` and tracking lookup.
- **UI Components**: `OrderConfirmationComponent`, `OrderTrackingComponent`, `PaymentReceiptComponent`.
- **Acceptance Criteria**:
  - Searching a valid order number renders order items, delivery address, and payment status.
  - Updating payment status to `COMPLETED` reflects immediately on the order confirmation view.

---

### Phase 8: Customer Dashboard & Order History
- **Objective**: Provide customers full visibility over their past purchases and self-service cancellation.
- **Features**:
  - Customer Order History page (`/account/orders`).
  - Order Detail view showing itemized line items and current status badge.
  - Self-service "Cancel Order" action:
    - Enabled for `PENDING`, `CONFIRMED`, `PROCESSING`.
    - Disabled for `SHIPPED`, `DELIVERED`, `CANCELLED`.
  - Customer Profile Edit page (`/account/profile`).
- **Backend APIs Required**:
  - `GET /api/v1/orders?customerId={id}`
  - `PUT /api/v1/orders/{id}/cancel`
  - `PUT /api/v1/customers/{id}`
- **NgRx Requirements**: `orderAdapter` managing customer orders list.
- **UI Components**: `OrderHistoryComponent`, `OrderCardComponent`, `CancelOrderDialogComponent`, `ProfileEditComponent`.
- **Acceptance Criteria**:
  - Cancelling an order updates the status pill to `CANCELLED` and displays a restocked notification.
  - Terminal orders disable the cancel action.

---

### Phase 9: Administrative Console
- **Objective**: Equip administrators with catalog and order fulfillment tools.
- **Features**:
  - Admin Dashboard with metrics: total revenue, active orders, total catalog items.
  - Product Management table with search, category filtering, and delete confirmation.
  - Product Create & Edit Reactive Form with SKU uniqueness validation.
  - Category Management view with inline add/edit dialogs.
  - Order Fulfillment Board with status advancement action buttons (`Confirm`, `Process`, `Ship`, `Deliver`).
- **Backend APIs Required**:
  - All Category CRUD: `POST`, `PUT`, `DELETE /api/v1/categories/**`
  - All Product CRUD: `POST`, `PUT`, `DELETE /api/v1/products/**`
  - All Order Status: `PUT /api/v1/orders/{id}/status`
  - Customer Directory: `GET /api/v1/customers`
- **UI Components**: `AdminLayoutComponent`, `AdminDashboardComponent`, `AdminProductListComponent`, `AdminProductFormComponent`, `AdminOrderFulfillmentComponent`.
- **Acceptance Criteria**:
  - Advancing an order enforces the backend state machine; invalid transitions display a friendly error banner.
  - Creating a product with an existing SKU correctly displays the `409 Conflict` field validation error.

---

### Phase 10: Testing, Performance Optimization & Production Readiness
- **Objective**: Ensure enterprise code quality, high Lighthouse scores, and deployment readiness.
- **Features**:
  - Unit testing for services, reducers, and selectors using Jasmine/Karma or Jest.
  - End-to-end testing with Cypress or Playwright covering the critical checkout funnel.
  - Angular Route Lazy Loading for feature modules (`/products`, `/cart`, `/checkout`, `/admin`).
  - Lighthouse performance optimization (image optimization, tree shaking, bundle budget checks under 500kb initial).
  - Production environment configuration (`environment.prod.ts`) with production API URL.
- **Acceptance Criteria**:
  - Unit test coverage exceeds 80% on NgRx reducers and selectors.
  - Lighthouse performance score >= 90 on mobile.
  - E2E test runs full customer journey: Home -> PLP -> PDP -> Cart -> Checkout -> Order Confirmed.
