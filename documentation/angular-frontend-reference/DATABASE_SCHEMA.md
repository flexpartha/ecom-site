# Database Schema & Entity Reference

## 1. Overview & Database Architecture

The relational database for `ecommerce-api` is named `ecommerce_db` running on MySQL 8.x with `utf8mb4` encoding and `utf8mb4_unicode_ci` collation. 

Spring Boot is configured with `spring.jpa.hibernate.ddl-auto=validate`, meaning Hibernate does **not** generate or alter tables at runtime. The authoritative schema is defined in `db/schema.sql`.

---

## 2. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    categories ||--o{ products : "has many (1:N)"
    customers ||--o{ orders : "places (1:N)"
    orders ||--|{ order_items : "contains (1:N)"
    products ||--o{ order_items : "referenced by (1:N)"
    orders ||--|| payments : "has one (1:1)"

    categories {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(100) name UK "NOT NULL"
        VARCHAR(100) slug UK "NOT NULL"
        TEXT description "NULL"
        DATETIME created_at "NULL"
        DATETIME updated_at "NULL"
    }

    products {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(200) name "NOT NULL"
        TEXT description "NULL"
        DECIMAL(10,2) price "NOT NULL"
        VARCHAR(100) sku UK "NOT NULL"
        INT stock_quantity "NOT NULL, DEFAULT 0"
        BOOLEAN active "NOT NULL, DEFAULT TRUE"
        BIGINT category_id FK "NOT NULL"
        DATETIME created_at "NULL"
        DATETIME updated_at "NULL"
    }

    customers {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(100) first_name "NOT NULL"
        VARCHAR(100) last_name "NOT NULL"
        VARCHAR(255) email UK "NOT NULL"
        VARCHAR(20) phone "NULL"
        TEXT address "NULL"
        DATETIME created_at "NULL"
    }

    orders {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(20) order_number UK "NOT NULL"
        BIGINT customer_id FK "NOT NULL"
        VARCHAR(20) status "NOT NULL, DEFAULT 'PENDING'"
        DECIMAL(12,2) total_amount "NOT NULL, DEFAULT 0.00"
        TEXT shipping_address "NOT NULL"
        TEXT notes "NULL"
        DATETIME created_at "NULL"
        DATETIME updated_at "NULL"
    }

    order_items {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT order_id FK "NOT NULL"
        BIGINT product_id FK "NOT NULL"
        INT quantity "NOT NULL"
        DECIMAL(10,2) unit_price "NOT NULL"
        DECIMAL(12,2) subtotal "NOT NULL"
    }

    payments {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT order_id FK, UK "NOT NULL"
        VARCHAR(30) payment_method "NOT NULL"
        VARCHAR(20) payment_status "NOT NULL, DEFAULT 'PENDING'"
        DECIMAL(12,2) amount "NOT NULL"
        DATETIME created_at "NULL"
    }
```

---

## 3. Table Specifications & Column Details

### 3.1 Table: `categories`
Maps to Java Entity: `com.ecom.api.entity.Category`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Category identifier |
| `name` | `VARCHAR(100)` | `String` | **No** | `UNIQUE KEY (uk_categories_name)` | Unique category display name |
| `slug` | `VARCHAR(100)` | `String` | **No** | `UNIQUE KEY (uk_categories_slug)` | URL-friendly unique slug |
| `description` | `TEXT` | `String` | Yes | - | Category description |
| `created_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PrePersist` | Timestamp when record was created |
| `updated_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PreUpdate` | Timestamp when record was last updated |

**JPA Relationships**:
- `@OneToMany(mappedBy = "category") List<Product> products`: One category has many products.

---

### 3.2 Table: `products`
Maps to Java Entity: `com.ecom.api.entity.Product`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Product identifier |
| `name` | `VARCHAR(200)` | `String` | **No** | - | Product title/name |
| `description` | `TEXT` | `String` | Yes | - | Detailed product description |
| `price` | `DECIMAL(10,2)`| `BigDecimal` | **No** | Must be > 0.00 | Unit retail price |
| `sku` | `VARCHAR(100)` | `String` | **No** | `UNIQUE KEY (uk_products_sku)` | Stock Keeping Unit (e.g. `ELEC-MOU-001`) |
| `stock_quantity`| `INT` | `Integer` | **No** | `DEFAULT 0` | Available stock count |
| `active` | `BOOLEAN` | `Boolean` | **No** | `DEFAULT TRUE` | Visibility flag (inactive cannot be ordered) |
| `category_id` | `BIGINT` | `Category` | **No** | `FOREIGN KEY REFERENCES categories(id)` | Parent category ID |
| `created_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PrePersist` | Timestamp when product was created |
| `updated_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PreUpdate` | Timestamp when product was updated |

**Indexes**:
- `uk_products_sku` (`sku`) - UNIQUE
- `idx_products_category` (`category_id`) - B-Tree Index
- `idx_products_active` (`active`) - B-Tree Index

**JPA Relationships**:
- `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") Category category`: Many products belong to one category.

---

### 3.3 Table: `customers`
Maps to Java Entity: `com.ecom.api.entity.Customer`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Customer identifier |
| `first_name` | `VARCHAR(100)` | `String` | **No** | - | Customer's first name |
| `last_name` | `VARCHAR(100)` | `String` | **No** | - | Customer's last name |
| `email` | `VARCHAR(255)` | `String` | **No** | `UNIQUE KEY (uk_customers_email)` | Customer's email address |
| `phone` | `VARCHAR(20)` | `String` | Yes | Max 20 chars | Optional contact number |
| `address` | `TEXT` | `String` | Yes | - | Default physical address text |
| `created_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PrePersist` | Timestamp when registered |

**Important Architectural Note for Angular**:
- Notice that `customers` does **not** contain a `password` column. The backend does not implement user passwords or account credentials in the current database version.

**JPA Relationships**:
- `@OneToMany(mappedBy = "customer") List<Order> orders`: One customer can have multiple historical orders.

---

### 3.4 Table: `orders`
Maps to Java Entity: `com.ecom.api.entity.Order`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Order primary key |
| `order_number` | `VARCHAR(20)` | `String` | **No** | `UNIQUE KEY (uk_orders_order_number)` | Public order code (e.g., `ORD-7E3B1A2C`) |
| `customer_id` | `BIGINT` | `Customer` | **No** | `FOREIGN KEY REFERENCES customers(id)` | Associated customer ID |
| `status` | `VARCHAR(20)` | `OrderStatus` | **No** | `DEFAULT 'PENDING'` | Current lifecycle stage (Enum) |
| `total_amount` | `DECIMAL(12,2)`| `BigDecimal` | **No** | `DEFAULT 0.00` | Sum of item subtotals |
| `shipping_address`| `TEXT` | `String` | **No** | Mandatory text | Delivery destination address |
| `notes` | `TEXT` | `String` | Yes | - | Optional customer instructions |
| `created_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PrePersist` | Order timestamp |
| `updated_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PreUpdate` | Timestamp of last status change |

**Indexes**:
- `uk_orders_order_number` (`order_number`) - UNIQUE
- `idx_orders_customer` (`customer_id`) - B-Tree Index
- `idx_orders_status` (`status`) - B-Tree Index

**JPA Relationships**:
- `@ManyToOne(fetch = FetchType.LAZY) Customer customer`
- `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) List<OrderItem> items`
- `@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) Payment payment`

---

### 3.5 Table: `order_items`
Maps to Java Entity: `com.ecom.api.entity.OrderItem`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Order line item ID |
| `order_id` | `BIGINT` | `Order` | **No** | `FOREIGN KEY REFERENCES orders(id)` | Parent order ID |
| `product_id` | `BIGINT` | `Product` | **No** | `FOREIGN KEY REFERENCES products(id)`| Purchased product ID |
| `quantity` | `INT` | `Integer` | **No** | Must be >= 1 | Units purchased |
| `unit_price` | `DECIMAL(10,2)`| `BigDecimal` | **No** | Locked price at time of purchase | Snapshot unit price |
| `subtotal` | `DECIMAL(12,2)`| `BigDecimal` | **No** | `quantity * unit_price` | Line item total amount |

**Indexes**:
- `idx_order_items_order` (`order_id`) - B-Tree Index
- `idx_order_items_product` (`product_id`) - B-Tree Index

**JPA Relationships**:
- `@ManyToOne(fetch = FetchType.LAZY) Order order`
- `@ManyToOne(fetch = FetchType.LAZY) Product product`

---

### 3.6 Table: `payments`
Maps to Java Entity: `com.ecom.api.entity.Payment`

| Column Name | SQL Type | Java Type | Nullable | Constraints & Defaults | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `Long` | **No** | `PRIMARY KEY AUTO_INCREMENT` | Payment record ID |
| `order_id` | `BIGINT` | `Order` | **No** | `FOREIGN KEY REFERENCES orders(id), UNIQUE` | Exactly 1 payment per order |
| `payment_method`| `VARCHAR(30)` | `PaymentMethod`| **No** | Enum string representation | Selected payment instrument |
| `payment_status`| `VARCHAR(20)` | `PaymentStatus`| **No** | `DEFAULT 'PENDING'` | State of transaction (Enum) |
| `amount` | `DECIMAL(12,2)`| `BigDecimal` | **No** | Equals order total amount | Amount payable/paid |
| `created_at` | `DATETIME` | `LocalDateTime`| Yes | Auto-set on `@PrePersist` | Payment initialization timestamp |

**Indexes**:
- `uk_payments_order_id` (`order_id`) - UNIQUE

**JPA Relationships**:
- `@OneToOne(fetch = FetchType.LAZY) Order order`

---

## 4. Enumerations & Value Domains

### 4.1 `OrderStatus` (`com.ecom.api.entity.OrderStatus`)
Stored as `VARCHAR(20)` in `orders.status`:
- `PENDING`: Initial status when order is created and saved.
- `CONFIRMED`: Order has been validated and confirmed.
- `PROCESSING`: Order items are being packed or readied.
- `SHIPPED`: Order has been dispatched with a carrier.
- `DELIVERED`: Order has successfully reached the customer (Terminal).
- `CANCELLED`: Order was cancelled prior to shipment; items returned to stock (Terminal).
- `REFUNDED`: Customer payment has been refunded (Terminal).

### 4.2 `PaymentMethod` (`com.ecom.api.entity.PaymentMethod`)
Stored as `VARCHAR(30)` in `payments.payment_method`:
- `CREDIT_CARD`: Visa / Mastercard / Amex Credit Card.
- `DEBIT_CARD`: Bank Debit Card.
- `UPI`: Unified Payments Interface (Google Pay, PhonePe, Paytm).
- `NET_BANKING`: Direct Internet Banking transfer.
- `WALLET`: Digital Wallets (Apple Pay, PayPal, etc.).
- `CASH_ON_DELIVERY`: Cash collected on physical receipt.

### 4.3 `PaymentStatus` (`com.ecom.api.entity.PaymentStatus`)
Stored as `VARCHAR(20)` in `payments.payment_status`:
- `PENDING`: Payment initialized but not yet confirmed.
- `COMPLETED`: Payment successfully processed.
- `FAILED`: Payment processing rejected or expired.
- `REFUNDED`: Payment reversed and returned to customer.

---

## 5. Non-Existent Tables & Structural Limitations

The following tables are **not present** in the database schema:
1. `users` / `accounts` / `roles` / `permissions`: No user credential or authorization tables.
2. `carts` / `cart_items`: Shopping carts are not persisted in MySQL.
3. `wishlists` / `wishlist_items`: Wishlists are not persisted.
4. `addresses`: No multi-address normalization table; address is stored as raw TEXT inside `customers` and `orders`.
5. `reviews` / `ratings`: No product review tables.
6. `coupons` / `discounts`: No promotional discount tables.
7. `shipments` / `tracking`: No shipping tracking tables.
