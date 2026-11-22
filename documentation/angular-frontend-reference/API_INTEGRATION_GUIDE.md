# Angular Developer Integration Guide

## 1. Quick Start & Connection Parameters

| Parameter | Development | Production (Reference) |
| :--- | :--- | :--- |
| **Backend Base URL** | `http://localhost:8080/api/v1` | `https://api.ecommerce.example.com/api/v1` |
| **Angular Dev Server** | `http://localhost:4200` | Static CDN / S3 / Nginx |
| **OpenAPI / Swagger** | `http://localhost:8080/swagger-ui/index.html` | `/swagger-ui/index.html` |
| **Actuator Health** | `http://localhost:8080/actuator/health` | `/actuator/health` |

---

## 2. Critical Network & CORS Setup

### 2.1 The CORS Reality & Solution
The Spring Boot application **does configure a global `CorsConfigurationSource`**:
- **Allowed Origins**: `http://localhost:4200`
- **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`
- **Allowed Headers**: `*`
- **Exposed Headers**: `Authorization`
- **Allow Credentials**: `true`
- **Max Age**: 3600 seconds

This means direct browser requests from `http://localhost:4200` to `http://localhost:8080` are permitted and authenticated.

### 2.2 Angular Development Proxy (`proxy.conf.json`) (Optional)
For path restructuring or additional dev-server control, you may still configure the Angular CLI proxy:
> **Note**: This is no longer strictly required for CORS, but is useful for proxies or rewrites.

To bypass CORS in local development without altering the Java codebase, configure the Angular CLI proxy:

Create `proxy.conf.json` in the root of your Angular project:
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

Update `angular.json`:
```json
"serve": {
  "builder": "@angular-devkit/build-angular:dev-server",
  "configurations": {
    "development": {
      "proxyConfig": "proxy.conf.json"
    }
  }
}
```

Now, Angular `HttpClient` calls simply use `/api/v1/...` without hardcoding `http://localhost:8080`.

---

## 3. Data Formatting & Conventions

### 3.1 JSON Naming Conventions
- **JSON Property Names**: `camelCase` (e.g., `stockQuantity`, `categoryId`, `orderNumber`, `paymentMethod`).
- **Database Column Names**: `snake_case` (e.g., `stock_quantity`, `category_id`, `order_number`).
- Note: Spring Boot automatically converts between `camelCase` DTOs and `snake_case` columns via Hibernate naming strategy. Angular code should strictly use `camelCase`.

### 3.2 Date & Time Formats
- Timestamps are serialized as ISO-8601 strings without timezone offset:
  `"2026-11-21T10:15:30"` (Java `LocalDateTime`).
- **Angular Parsing**:
  Use Angular's `DatePipe`: `{{ order.createdAt | date:'medium' }}`.

### 3.3 Currency & BigDecimal Numbers
- Prices, totals, and monetary values are Java `BigDecimal` formatted in JSON as numeric decimals (e.g. `24.99`, `0.00`).
- **Angular Display**:
  Use Angular's `CurrencyPipe`: `{{ product.price | currency:'USD':'symbol':'1.2-2' }}`.

### 3.4 Product Images & Static Assets
- The current backend **does not** store image files or provide image upload endpoints. The `Product` entity has no `imageUrl` property.
- **Frontend Recommendation**:
  Map product images on the Angular side using a deterministic CDN pattern or placeholder utility based on product ID or SKU:
  ```typescript
  getProductImageUrl(product: Product): string {
    return `https://assets.example.com/products/${product.sku.toLowerCase()}.jpg`;
  }
  ```

---

## 4. Pagination & Sorting Conventions

- **Current Backend Reality**:
  - **No Pagination**: Endpoints like `GET /api/v1/products` and `GET /api/v1/orders` return an unbounded `List<T>`.
  - **No Sorting**: Results are returned in default database insertion or ID order.
- **Angular Handling Strategy**:
  Implement client-side pagination and sorting using RxJS or UI component libraries (e.g. Angular Material `MatPaginator` and `MatSort`):
  ```typescript
  // In Angular Component:
  displayedProducts$ = this.products$.pipe(
    map(products => products.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize))
  );
  ```

---

## 5. Standard Error Handling Pattern

The backend returns a uniform `ErrorResponse` structure for all HTTP 4xx and 5xx errors:

```typescript
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: Record<string, string>; // Present on 400 Bad Request
}
```

### 5.1 Global Angular Error Interceptor (`error.interceptor.ts`)

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class GlobalHttpErrorInterceptor implements HttpInterceptor {

  constructor(private snackBar: MatSnackBar) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let displayMessage = 'An unexpected error occurred';

        if (error.error && typeof error.error === 'object') {
          const errBody = error.error;
          if (errBody.errors && Object.keys(errBody.errors).length > 0) {
            // Validation errors: display first field error
            const firstField = Object.keys(errBody.errors)[0];
            displayMessage = `${firstField}: ${errBody.errors[firstField]}`;
          } else if (errBody.message) {
            displayMessage = errBody.message;
          }
        }

        switch (error.status) {
          case 400:
            this.snackBar.open(`Validation Error: ${displayMessage}`, 'Close', { duration: 5000 });
            break;
          case 404:
            this.snackBar.open(`Resource Not Found: ${displayMessage}`, 'Close', { duration: 4000 });
            break;
          case 409:
            this.snackBar.open(`Conflict: ${displayMessage}`, 'Close', { duration: 5000 });
            break;
          case 422:
            this.snackBar.open(`Business Rule Violation: ${displayMessage}`, 'Close', { duration: 6000 });
            break;
          case 500:
            this.snackBar.open('Server Error. Please try again later.', 'Close', { duration: 5000 });
            break;
        }

        return throwError(() => error);
      })
    );
  }
}
```

---

## 6. Concrete Angular Service Implementations

### 6.1 `ProductService` (`product.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductResponse, ProductRequest } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly baseUrl = '/api/v1/products';

  constructor(private http: HttpClient) {}

  getAllProducts(categoryId?: number, active?: boolean): Observable<ProductResponse[]> {
    let params = new HttpParams();
    if (categoryId !== undefined && categoryId !== null) {
      params = params.set('categoryId', categoryId.toString());
    }
    if (active !== undefined && active !== null) {
      params = params.set('active', active.toString());
    }
    return this.http.get<ProductResponse[]>(this.baseUrl, { params });
  }

  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.baseUrl}/${id}`);
  }

  getProductBySku(sku: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.baseUrl}/sku/${encodeURIComponent(sku)}`);
  }

  createProduct(request: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.baseUrl, request);
  }

  updateProduct(id: number, request: ProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.baseUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
```

### 6.2 `OrderService` (`order.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderResponse, CreateOrderRequest, UpdateOrderStatusRequest } from '../models/order.model';
import { OrderStatus } from '../models/enums.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly baseUrl = '/api/v1/orders';

  constructor(private http: HttpClient) {}

  createOrder(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.baseUrl, request);
  }

  getAllOrders(customerId?: number, status?: OrderStatus): Observable<OrderResponse[]> {
    let params = new HttpParams();
    if (customerId) {
      params = params.set('customerId', customerId.toString());
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<OrderResponse[]>(this.baseUrl, { params });
  }

  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`);
  }

  getOrderByOrderNumber(orderNumber: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/number/${encodeURIComponent(orderNumber)}`);
  }

  updateOrderStatus(id: number, status: OrderStatus): Observable<OrderResponse> {
    const payload: UpdateOrderStatusRequest = { status };
    return this.http.put<OrderResponse>(`${this.baseUrl}/${id}/status`, payload);
  }

  cancelOrder(id: number): Observable<OrderResponse> {
    return this.http.put<OrderResponse>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
```

### 6.3 `PaymentService` (`payment.service.ts`)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentResponse, UpdatePaymentStatusRequest } from '../models/payment.model';
import { PaymentStatus } from '../models/enums.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly baseUrl = '/api/v1/payments';

  constructor(private http: HttpClient) {}

  getPaymentByOrderId(orderId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.baseUrl}/order/${orderId}`);
  }

  updatePaymentStatus(orderId: number, paymentStatus: PaymentStatus): Observable<PaymentResponse> {
    const payload: UpdatePaymentStatusRequest = { paymentStatus };
    return this.http.put<PaymentResponse>(`${this.baseUrl}/order/${orderId}/status`, payload);
  }
}
```

### 6.4 `AuthService` (`auth.service.ts`) — NEW

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest, SignupRequest, AuthResponse, SignupResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = '/api/v1';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request);
  }

  signup(request: SignupRequest): Observable<SignupResponse> {
    return this.http.post<SignupResponse>(`${this.baseUrl}/signup`, request);
  }
}
```

---

## 7. Angular HTTP Timeout & Retry Guidelines

- Set a default timeout of 10,000ms for read operations and 15,000ms for transactional checkout calls.
- Use RxJS `timeout` operator:
  ```typescript
  import { timeout, retry } from 'rxjs/operators';

  this.productService.getAllProducts().pipe(
    timeout(10000),
    retry({ count: 2, delay: 1000 })
  );
  ```
- **Important**: Do **not** automatically retry `POST /api/v1/orders` on timeout, because the order may have already been committed in the database, resulting in duplicate order creation and multiple stock deductions.
