# Frontend Feature-to-API Mapping Matrix

## 1. Feature Mapping Matrix Overview

This document provides a comprehensive mapping between user-facing Angular screens and the backend REST API endpoints, detailing required state, component dependencies, form validation rules, UI states (loading, empty, error), and access control.

---

## 2. Customer-Facing Screens

| Feature | Angular Route / Page | APIs Required | State Required | Backend Status |
| :--- | :--- | :--- | :--- | :---: |
| **Storefront Homepage** | `/` (`HomeComponent`) | `GET /api/v1/categories`<br>`GET /api/v1/products?active=true` | `CategoryState`<br>`ProductState` | **Supported** |
| **Category Showcase** | `/category/:slug` (`CategoryProductsComponent`) | `GET /api/v1/categories/slug/{slug}`<br>`GET /api/v1/products?categoryId={id}&active=true` | `CategoryState`<br>`ProductState` | **Supported** |
| **Product Catalog (PLP)** | `/products` (`ProductListComponent`) | `GET /api/v1/products?active=true`<br>`GET /api/v1/categories` | `ProductState`<br>`FilterCriteria` | **Supported** (Filtering client-side) |
| **Product Details (PDP)** | `/products/:id` (`ProductDetailComponent`) | `GET /api/v1/products/{id}` | `SelectedProduct`<br>`CartState` | **Supported** |
| **Shopping Cart** | `/cart` (`CartComponent`) | *None* | `CartState` (NgRx + localStorage) | **Client-Side State** |
| **Wishlist** | `/wishlist` (`WishlistComponent`) | *None* | `WishlistState` (NgRx + localStorage) | **Client-Side State** |
| **Checkout & Payment** | `/checkout` (`CheckoutComponent`) | `POST /api/v1/customers` (or lookup)<br>`POST /api/v1/orders`<br>`PUT /api/v1/payments/order/{id}/status` | `CartState`<br>`AuthState`<br>`CheckoutState` | **Supported** |
| **Order Confirmation** | `/orders/:orderNumber/confirmed` (`OrderSuccessComponent`) | `GET /api/v1/orders/number/{orderNumber}` | `CurrentOrder` | **Supported** |
| **Public Order Tracking** | `/track-order` (`TrackOrderComponent`) | `GET /api/v1/orders/number/{orderNumber}` | `TrackedOrder` | **Supported** |
| **Customer Order History**| `/account/orders` (`OrderHistoryComponent`) | `GET /api/v1/orders?customerId={id}`<br>`PUT /api/v1/orders/{id}/cancel` | `AuthState`<br>`OrderState` | **Supported** |
| **Customer Profile** | `/account/profile` (`ProfileComponent`) | `GET /api/v1/customers/{id}`<br>`PUT /api/v1/customers/{id}` | `AuthState` | **Supported** |

---

## 3. Administrative Screens

| Feature | Angular Route / Page | APIs Required | State Required | Backend Status |
| :--- | :--- | :--- | :--- | :---: |
| **Admin Dashboard Overview** | `/admin` (`AdminDashboardComponent`) | `GET /api/v1/orders`<br>`GET /api/v1/products`<br>`GET /api/v1/categories` | `AdminDashboardState` | **Supported** (Aggregated client-side) |
| **Product Management Table** | `/admin/products` (`AdminProductListComponent`) | `GET /api/v1/products`<br>`DELETE /api/v1/products/{id}` | `ProductState` | **Supported** |
| **Create Product Form** | `/admin/products/new` (`AdminProductFormComponent`) | `GET /api/v1/categories`<br>`POST /api/v1/products` | `CategoryState` | **Supported** |
| **Edit Product Form** | `/admin/products/:id/edit` (`AdminProductFormComponent`) | `GET /api/v1/categories`<br>`GET /api/v1/products/{id}`<br>`PUT /api/v1/products/{id}` | `CategoryState`<br>`SelectedProduct` | **Supported** |
| **Category Management** | `/admin/categories` (`AdminCategoryListComponent`) | `GET /api/v1/categories`<br>`POST /api/v1/categories`<br>`PUT /api/v1/categories/{id}`<br>`DELETE /api/v1/categories/{id}` | `CategoryState` | **Supported** |
| **Order Fulfillment** | `/admin/orders` (`AdminOrderListComponent`) | `GET /api/v1/orders?status={status}`<br>`PUT /api/v1/orders/{id}/status`<br>`PUT /api/v1/payments/order/{id}/status` | `OrderState` | **Supported** |
| **Customer Directory** | `/admin/customers` (`AdminCustomerListComponent`) | `GET /api/v1/customers`<br>`DELETE /api/v1/customers/{id}` | `CustomerState` | **Supported** |

---

## 4. Detailed Screen Specifications

### 4.1 Product Catalog Listing (PLP)
- **Route**: `/products`
- **APIs Consumed**:
  - `GET /api/v1/products?active=true`
  - `GET /api/v1/categories`
- **Forms & Inputs**:
  - Search input (debounced 300ms, filters title/description client-side).
  - Category dropdown or chip group.
  - Price range slider (min: $0, max: $500).
  - Sort select (Price Low-to-High, Price High-to-Low, Name A-Z).
- **UI States**:
  - **Loading**: Skeleton cards (shimmer effect) mimicking product cards.
  - **Empty**: "No products found matching your filter criteria" with a "Reset Filters" action button.
  - **Error**: "Failed to load product catalog. [Retry]" button.

---

### 4.2 Product Detail Page (PDP)
- **Route**: `/products/:id`
- **APIs Consumed**:
  - `GET /api/v1/products/{id}`
- **Interactions**:
  - Quantity selector (bounded between 1 and `product.stockQuantity`).
  - "Add to Cart" button (disabled if `stockQuantity === 0` or `active === false`).
  - "Add to Wishlist" heart toggle.
  - Real-time stock badge:
    - `stockQuantity > 10`: Green badge ("In Stock").
    - `1 <= stockQuantity <= 10`: Amber badge ("Only {x} left in stock - order soon").
    - `stockQuantity === 0`: Red badge ("Out of Stock").
- **UI States**:
  - **Loading**: Centered spinner or skeleton layout.
  - **Error (404)**: "Product not found. It may have been removed or discontinued. [Back to Shop]".

---

### 4.3 Shopping Cart Page
- **Route**: `/cart`
- **APIs Consumed**: None (Client-side NgRx state persisted in `localStorage`).
- **Functionality**:
  - Item list displaying thumbnail placeholder, title, unit price, quantity controls (`+` / `-`), line subtotal, and remove icon.
  - Order summary card displaying Subtotal, Estimated Tax (e.g. calculated at 8%), Shipping (Free over $50), and Estimated Total.
  - "Proceed to Checkout" primary CTA button.
- **UI States**:
  - **Empty**: Large cart icon with "Your shopping cart is empty. [Start Shopping]" button.

---

### 4.4 Checkout Page
- **Route**: `/checkout`
- **APIs Consumed**:
  - `POST /api/v1/customers` (If new customer) OR `GET /api/v1/customers/{id}`
  - `POST /api/v1/orders`
  - `PUT /api/v1/payments/order/{orderId}/status` (Simulated immediate payment)
- **Form Groups**:
  1. **Customer Information Form**:
     - `firstName`: Required, max 100.
     - `lastName`: Required, max 100.
     - `email`: Required, valid email regex.
     - `phone`: Optional, max 20.
  2. **Shipping Address Form**:
     - `shippingAddress`: Required textarea.
     - `notes`: Optional textarea (delivery instructions).
  3. **Payment Method Selection**:
     - Radio group: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `WALLET`, `CASH_ON_DELIVERY`.
     - Mock card input form for card methods.
- **UI States**:
  - **Submitting**: Full-screen overlay with progress indicator ("Securing inventory and placing your order...").
  - **Stock Error (422)**: Inline banner stating which product had insufficient stock with a button to adjust cart quantity.

---

### 4.5 Customer Order History & Cancellation
- **Route**: `/account/orders`
- **APIs Consumed**:
  - `GET /api/v1/orders?customerId={currentCustomer.id}`
  - `PUT /api/v1/orders/{id}/cancel`
- **Features**:
  - Expandable order cards showing order number, creation date, status pill, item list, and total amount.
  - "Cancel Order" button visible **only** if order status is `PENDING`, `CONFIRMED`, or `PROCESSING`.
  - Confirmation dialog before dispatching cancellation request.
- **UI States**:
  - **Empty**: "You haven't placed any orders yet. [Explore Products]".

---

### 4.6 Admin Product Management & Form
- **Routes**: `/admin/products`, `/admin/products/new`, `/admin/products/:id/edit`
- **APIs Consumed**:
  - `GET /api/v1/products`
  - `GET /api/v1/categories`
  - `POST /api/v1/products` (Create)
  - `PUT /api/v1/products/{id}` (Update)
  - `DELETE /api/v1/products/{id}` (Delete)
- **Validation Rules (Reactive Form)**:
  - `name`: `Validators.required`, `Validators.maxLength(200)`.
  - `sku`: `Validators.required`, `Validators.maxLength(100)`.
  - `price`: `Validators.required`, `Validators.min(0.01)`.
  - `stockQuantity`: `Validators.required`, `Validators.min(0)`.
  - `categoryId`: `Validators.required`.
  - `active`: Boolean toggle.
- **Error Handling**:
  - `409 Conflict`: Show inline error under SKU input ("SKU already exists in catalog").

---

### 4.7 Admin Order Fulfillment Board
- **Route**: `/admin/orders`
- **APIs Consumed**:
  - `GET /api/v1/orders` (filter by status tab)
  - `PUT /api/v1/orders/{id}/status`
  - `PUT /api/v1/payments/order/{orderId}/status`
- **Functionality**:
  - Tabbed or Kanban view by status (`PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
  - Action buttons to advance state:
    - On `PENDING`: "Confirm Order" -> advances to `CONFIRMED`.
    - On `CONFIRMED`: "Begin Processing" -> advances to `PROCESSING`.
    - On `PROCESSING`: "Dispatch & Ship" -> advances to `SHIPPED`.
    - On `SHIPPED`: "Mark Delivered" -> advances to `DELIVERED`.
    - On any active state: "Cancel & Restock" -> cancels order and returns stock.
  - Payment status toggle to mark `PENDING` -> `COMPLETED` or `REFUNDED`.
