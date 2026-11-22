# Angular TypeScript Data Models & DTO Interfaces

## 1. Overview

This document provides ready-to-use TypeScript interfaces, types, and enums matching the Java backend DTOs and domain entities in `com.ecom.api.dto` and `com.ecom.api.entity`.

Each interface strictly matches the serialized JSON fields, handling nullability, optional fields, and type conversions (e.g. `BigDecimal` -> `number`, `LocalDateTime` -> `string`).

---

## 2. Common Domain Enums (`enums.model.ts`)

```typescript
/**
 * Mirrors com.ecom.api.entity.OrderStatus
 */
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

/**
 * Mirrors com.ecom.api.entity.PaymentMethod
 */
export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  UPI = 'UPI',
  NET_BANKING = 'NET_BANKING',
  WALLET = 'WALLET',
  CASH_ON_DELIVERY = 'CASH_ON_DELIVERY'
}

/**
 * Mirrors com.ecom.api.entity.PaymentStatus
 */
export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}
```

---

## 3. Category Models (`category.model.ts`)

```typescript
/**
 * Mirrors com.ecom.api.dto.CategoryRequest
 */
export interface CategoryRequest {
  name: string;        // Required, max 100 chars, unique
  slug: string;        // Required, max 100 chars, unique
  description?: string | null;
}

/**
 * Mirrors com.ecom.api.dto.CategoryResponse
 */
export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  createdAt: string;   // ISO-8601 string, e.g. "2026-11-21T09:00:00"
  updatedAt: string;   // ISO-8601 string
}
```

---

## 4. Product Models (`product.model.ts`)

```typescript
/**
 * Mirrors com.ecom.api.dto.ProductRequest
 */
export interface ProductRequest {
  name: string;              // Required, max 200 chars
  description?: string | null;
  price: number;             // Required, must be > 0.00
  sku: string;               // Required, max 100 chars, unique
  stockQuantity: number;     // Required, min 0
  active?: boolean;          // Optional, defaults to true
  categoryId: number;        // Required, foreign key to Category
}

/**
 * Mirrors com.ecom.api.dto.ProductResponse
 */
export interface ProductResponse {
  id: number;
  name: string;
  description: string | null;
  price: number;             // BigDecimal serialized as numeric float
  sku: string;
  stockQuantity: number;
  active: boolean;
  categoryId: number;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}
```

---

## 5. Customer Models (`customer.model.ts`)

```typescript
/**
 * Mirrors com.ecom.api.dto.CustomerRequest
 */
export interface CustomerRequest {
  firstName: string;   // Required, max 100 chars
  lastName: string;    // Required, max 100 chars
  email: string;       // Required, valid email, max 255 chars, unique
  phone?: string | null; // Max 20 chars
  address?: string | null;
}

/**
 * Mirrors com.ecom.api.dto.CustomerResponse
 */
export interface CustomerResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  address: string | null;
  createdAt: string;
}
```

---

## 6. Authentication Models (`auth.model.ts`) — NEW in commit 374815b

```typescript
/**
 * Mirrors com.ecom.api.dto.LoginRequest
 * POST /api/v1/login or POST /api/v1/auth/login
 */
export interface LoginRequest {
  username: string;    // Required, not blank
  password: string;    // Required, not blank
}

/**
 * Mirrors com.ecom.api.dto.SignupRequest
 * POST /api/v1/signup or POST /api/v1/auth/signup
 */
export interface SignupRequest {
  firstName: string;   // Required, max 100 chars
  lastName: string;    // Required, max 100 chars
  username: string;    // Required, max 100 chars, unique
  password: string;    // Required, min 6 chars
}

/**
 * Mirrors com.ecom.api.dto.AuthResponse
 * Returned by POST /api/v1/login
 */
export interface AuthResponse {
  token: string;           // JWT access token
  tokenType: string;       // "Bearer"
  firstName: string;
  lastName: string;
}

/**
 * Mirrors com.ecom.api.dto.SignupResponse
 * Returned by POST /api/v1/signup
 */
export interface SignupResponse {
  status: number;          // HTTP status code (e.g., 200)
  message: string;         // "Registration successful"
}

/**
 * Mirrors com.ecom.api.entity.User
 * Used for user profile display (if profile endpoint added)
 */
export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Client-side Auth State for NgRx
 */
export interface AuthState {
  token: string | null;
  tokenType: string | null;
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
```

---

## 7. Order & Checkout Models (`order.model.ts`)

```typescript
import { OrderStatus, PaymentMethod } from './enums.model';
import { PaymentResponse } from './payment.model';

/**
 * Mirrors com.ecom.api.dto.OrderItemRequest
 */
export interface OrderItemRequest {
  productId: number;   // Required
  quantity: number;    // Required, min 1
}

/**
 * Mirrors com.ecom.api.dto.CreateOrderRequest
 */
export interface CreateOrderRequest {
  customerId: number;               // Required
  items: OrderItemRequest[];        // Required, min 1 item
  shippingAddress: string;          // Required, text
  notes?: string | null;            // Optional
  paymentMethod: PaymentMethod;     // Required
}

/**
 * Mirrors com.ecom.api.dto.OrderItemResponse
 */
export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

/**
 * Mirrors com.ecom.api.dto.OrderResponse
 */
export interface OrderResponse {
  id: number;
  orderNumber: string;              // e.g. "ORD-3F8E1A2B"
  customerId: number;
  customerName: string;
  customerEmail: string;
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: string;
  notes: string | null;
  items: OrderItemResponse[];
  payment: PaymentResponse | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Mirrors com.ecom.api.dto.UpdateOrderStatusRequest
 */
export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}
```

---

## 8. Payment Models (`payment.model.ts`)

```typescript
import { PaymentMethod, PaymentStatus } from './enums.model';

/**
 * Mirrors com.ecom.api.dto.PaymentResponse
 */
export interface PaymentResponse {
  id: number;
  orderId: number;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  amount: number;
  createdAt: string;
}

/**
 * Mirrors com.ecom.api.dto.UpdatePaymentStatusRequest
 */
export interface UpdatePaymentStatusRequest {
  paymentStatus: PaymentStatus;
}
```

---

## 9. Error Response Model (`error.model.ts`)

```typescript
/**
 * Mirrors com.ecom.api.dto.ErrorResponse
 */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: Record<string, string>; // Present on 400 Bad Request validation failures
}
```

---

## 10. Client-Side Only Models (Required for Angular Frontend)

The backend has no database tables or DTOs for carts, wishlists, or auth tokens. Angular must declare these client-side models:

### 10.1 Shopping Cart State (`cart.model.ts`)

```typescript
import { ProductResponse } from './product.model';

export interface CartItem {
  product: ProductResponse;
  quantity: number;
  subtotal: number;
}

export interface CartState {
  items: CartItem[];
  totalQuantity: number;
  totalAmount: number;
  lastUpdated: number;
}
```

### 10.2 Wishlist State (`wishlist.model.ts`)

```typescript
import { ProductResponse } from './product.model';

export interface WishlistState {
  items: ProductResponse[];
}
```

### 10.3 Client-Side Filter Criteria (`filter.model.ts`)

```typescript
export interface ProductFilterCriteria {
  categoryId?: number | null;
  active?: boolean | null;
  searchKeyword?: string;     // Handled via client-side filtering
  minPrice?: number | null;   // Handled via client-side filtering
  maxPrice?: number | null;   // Handled via client-side filtering
  sortBy?: 'price-asc' | 'price-desc' | 'name-asc' | 'newest';
}
```

---

## 11. Summary of Backend Model Availability

| Concept | TypeScript Model Defined | Backend DTO Equivalent |
| :--- | :---: | :---: |
| **Category** | `CategoryRequest`, `CategoryResponse` | `CategoryRequest`, `CategoryResponse` |
| **Product** | `ProductRequest`, `ProductResponse` | `ProductRequest`, `ProductResponse` |
| **Customer** | `CustomerRequest`, `CustomerResponse` | `CustomerRequest`, `CustomerResponse` |
| **Order** | `CreateOrderRequest`, `OrderResponse`, `UpdateOrderStatusRequest` | `CreateOrderRequest`, `OrderResponse`, `UpdateOrderStatusRequest` |
| **Order Line Item** | `OrderItemRequest`, `OrderItemResponse` | `OrderItemRequest`, `OrderItemResponse` |
| **Payment** | `PaymentResponse`, `UpdatePaymentStatusRequest` | `PaymentResponse`, `UpdatePaymentStatusRequest` |
| **Errors** | `ApiErrorResponse` | `ErrorResponse` |
| **Authentication** | `LoginRequest`, `SignupRequest`, `AuthResponse`, `SignupResponse`, `UserResponse` | `LoginRequest`, `SignupRequest`, `AuthResponse`, `SignupResponse`, `User` |
| **Cart** | `CartItem`, `CartState` | **None** (Client-side only) |
| **Wishlist** | `WishlistState` | **None** (Client-side only) |
| **Reviews / Coupons**| *Not present in backend* | **None** (Target architecture only) |