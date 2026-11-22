# UI/UX Architecture & Design System Specification

## 1. UI Framework Evaluation & Recommendation

To deliver an e-commerce experience comparable to modern premium platforms (such as Apple, Allbirds, or Nordstrom), we evaluated three UI architecture options:

| Criteria | Angular Material Only | Bootstrap 5 Only | Recommended: Angular Material + Tailwind/Bootstrap Utilities |
| :--- | :---: | :---: | :---: |
| **Accessibility (a11y)** | Exceptional (WCAG 2.1 AA) | Basic | **Exceptional** (via Material CDK) |
| **E-Commerce Visual Aesthetics** | Can look too "enterprise/admin" | Looks generic/dated out of box | **Custom, luxury, bespoke styling** |
| **Complex Components** | Dialogs, Stepper, Autocomplete | Modals, Collapse | **MatStepper, MatDialog, MatMenu** |
| **Layout Flexibility** | Limited grid system | Strong flexbox grid | **Dynamic responsive flexbox/grid** |
| **Animation & Micro-interactions**| Ripple, CDK overlays | CSS transitions | **Polished hardware-accelerated animations** |

### **The Recommendation: Angular Material + Utility-First CSS**
Use **Angular Material (v16+)** for complex interactive controls (Dialogs, Menus, Stepper, Badges, Autocomplete, Tooltips, Form Field Float Labels) combined with a **Utility CSS layer (Tailwind CSS or Bootstrap 5 Utilities)** for responsive grids, spacing, typography, and card layouts.

---

## 2. Design System Foundations

### 2.1 Color Palette
A refined, modern palette featuring deep slate neutrals, warm off-white grounds, and an emerald accent for high-converting e-commerce micro-moments.

```scss
// Primary Brand Colors
$color-brand-primary:    #0F172A; // Slate 900 (Deep Luxury Navy/Black)
$color-brand-secondary:  #334155; // Slate 700 (Subdued Charcoal)
$color-brand-accent:     #059669; // Emerald 600 (Conversion CTAs & In-Stock)
$color-brand-accent-hover:#047857; // Emerald 700

// Background & Surface
$color-surface-ground:   #F8FAFC; // Off-white clean page ground
$color-surface-card:     #FFFFFF; // Pure white card surfaces
$color-surface-elevated: #FFFFFF; // Dropdown & modal surfaces

// Text Hierarchy
$color-text-primary:     #0F172A; // 87% contrast
$color-text-secondary:   #64748B; // 60% contrast slate
$color-text-muted:       #94A3B8; // 38% contrast

// Status & Semantic Tokens
$color-status-success:   #10B981; // Green (Confirmed, Delivered)
$color-status-warning:   #F59E0B; // Amber (Processing, Low Stock)
$color-status-danger:    #EF4444; // Crimson (Cancelled, Failed, Out of Stock)
$color-status-info:      #3B82F6; // Blue (Shipped, Pending)
```

### 2.2 Typography
- **Primary Body & Interface**: `Inter`, `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`.
- **Editorial / Headings**: `Plus Jakarta Sans` or `Cabinet Grotesk` (clean, contemporary geometric sans-serif).
- **Scale**:
  - `Display 1`: 48px / Line Height 56px / Semi-bold (700)
  - `Heading 1`: 32px / Line Height 40px / Bold (700)
  - `Heading 2`: 24px / Line Height 32px / Semi-bold (600)
  - `Heading 3`: 20px / Line Height 28px / Semi-bold (600)
  - `Body Regular`: 15px / Line Height 24px / Regular (400)
  - `Body Small`: 13px / Line Height 20px / Regular (400)
  - `Caption / Badge`: 11px / Line Height 16px / Medium (500) Uppercase

### 2.3 Elevation & Shadows
- **Card Rest**: `0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.05)`
- **Card Hover**: `0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -4px rgba(0, 0, 0, 0.04)`
- **Modal / Flyout**: `0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05)`

### 2.4 Responsive Breakpoints
- **Mobile (`xs`)**: `< 576px`
- **Tablet Portrait (`sm`)**: `576px – 767px`
- **Tablet Landscape (`md`)**: `768px – 991px`
- **Desktop (`lg`)**: `992px – 1199px`
- **Large Display (`xl`)**: `>= 1200px` (Container max-width: `1280px`)

---

## 3. Storefront Component Specifications

### 3.1 Global Header & Navigation
- **Top Announcement Bar**: Subtle banner displaying shipping notes ("Free shipping on all orders over $50").
- **Primary Navbar**:
  - **Left**: Minimalist brand logo and Category dropdown menu (dynamically populated from `GET /api/v1/categories`).
  - **Center**: Predictive search bar with search icon and quick-clear `X`.
  - **Right**:
    - "Track Order" link (directs to public `/track-order`).
    - Wishlist icon with badge counter.
    - Shopping Cart button with badge counter (`selectTotalQuantity`) and slide-over mini-cart drawer trigger.
    - Customer Account menu (showing Customer Name or "Sign In / Register").

### 3.2 Category Mega Menu / Pills
- Below header: A horizontal, smoothly scrollable row of category pills (`All`, `Electronics`, `Books`, `Clothing`, `Home & Kitchen`).
- Active pill displays a filled slate background with white text; inactive pills show subtle borders with hover fill.

### 3.3 Storefront Homepage (`/`)
- **Hero Section**: High-impact banner with bold typography, lifestyle imagery, and a primary CTA button: "Shop New Arrivals".
- **Category Grid**: Visual cards with category titles, descriptions, and subtle zoom-on-hover effects.
- **Featured Products Section**: 4-column responsive grid displaying active products with badges ("In Stock", "Only 2 Left").
- **Trust Badges Row**: 4-column feature list: "Fast Delivery", "Secure Payments", "Easy Returns", "24/7 Support".

### 3.4 Product Listing Page (`/products`)
- **Two-Column Layout (Desktop)**:
  - **Left Sidebar Filter (280px)**:
    - Category radio list / checkboxes.
    - Price range slider with numeric min/max displays.
    - In-stock only toggle switch.
  - **Main Catalog (Flexible 3-4 Columns)**:
    - Top bar: Item count ("Showing 10 products") and "Sort By" dropdown (`Price: Low to High`, `Price: High to Low`, `Name: A-Z`).
    - Product Card Component:
      - 1:1 Aspect ratio image box with subtle border and rounded corners (`border-radius: 12px`).
      - Category chip label.
      - Product Title (clamped to 2 lines).
      - Bold price formatted via CurrencyPipe.
      - Stock badge.
      - "Add to Cart" button (full width on hover, or prominent quick-add `+` button).

### 3.5 Product Details Page (`/products/:id`)
- **Split Hero Layout (50/50 Desktop)**:
  - **Left**: High-resolution gallery viewport with thumbnail selector.
  - **Right (Sticky Buy Box)**:
    - Category breadcrumb (`Home > Electronics > Wireless Mouse`).
    - Product Title (`Heading 1`).
    - SKU code in subtle monospace font (`SKU: ELEC-MOU-001`).
    - Price display in 32px bold emerald text.
    - Stock Status Indicator:
      - Pulsing green dot: "In Stock (150 available)".
      - Pulsing orange dot: "Low Stock - Only 3 units remaining".
    - Quantity Stepper (`-` `[ 1 ]` `+`) bounded by available inventory.
    - Primary CTA: Large "Add to Shopping Cart" button.
    - Secondary CTA: "Add to Wishlist" outlined button.
    - Accordion Tabs: Product Description, Specifications, Shipping & Delivery Policy.

### 3.6 Slide-Over Mini-Cart & Cart Page (`/cart`)
- **Slide-Over Drawer**: Slides out from the right upon adding any item, preventing jarring page reloads.
- **Line Item Presentation**:
  - Thumbnail placeholder, title, SKU, unit price.
  - Inline quantity increment/decrement buttons.
  - Trash icon with fade-out animation.
- **Order Summary Box**:
  - Sticky right sidebar displaying Subtotal, Estimated Taxes, Shipping calculation, and Grand Total.
  - "Proceed to Checkout" primary button with lock icon ("100% Secure Checkout").

### 3.7 Checkout Experience (`/checkout`)
- **Multi-Step Stepper (`MatStepper` or Custom Linear Stepper)**:
  - **Step 1: Contact Information**:
    - Email address input with instant validation.
    - First name, Last name, Phone number.
  - **Step 2: Shipping Destination**:
    - Physical address textarea with clear helper text.
    - Special delivery notes.
  - **Step 3: Payment Selection**:
    - Method cards with logos: Credit/Debit Card, UPI, Net Banking, Wallet, Cash on Delivery.
  - **Step 4: Review & Place Order**:
    - Line item summary, totals breakdown, terms acceptance checkbox.
    - "Place Order" button.

### 3.8 Customer Account & Order History (`/account/orders`)
- **Order Card Design**:
  - Header: Order Number (`ORD-A1B2C3D4`), placement date, and Status Badge.
  - Body: Thumbnail strip of items, total amount, and shipping address.
  - Action footer:
    - "Track Package" button.
    - "Cancel Order" destructive button (disabled if already shipped or delivered).
    - Status-specific pill styling:
      - `PENDING`: Light amber ground, dark amber text.
      - `CONFIRMED`: Light blue ground, dark blue text.
      - `PROCESSING`: Light purple ground, dark purple text.
      - `SHIPPED`: Light indigo ground, dark indigo text.
      - `DELIVERED`: Light emerald ground, dark emerald text.
      - `CANCELLED`: Light rose ground, dark rose text.

---

## 4. Administrative Experience & Dashboard

### 4.1 Admin Shell Layout
- **Left Collapsible Navigation Drawer (260px)**:
  - Brand header with "Admin Console" badge.
  - Navigation links with icons:
    - Dashboard Overview (`/admin`)
    - Products Catalog (`/admin/products`)
    - Category Taxonomy (`/admin/categories`)
    - Orders & Fulfillment (`/admin/orders`)
    - Customers Directory (`/admin/customers`)
  - Bottom: "Switch to Storefront" button.

### 4.2 Admin Dashboard Overview (`/admin`)
- **Metric Cards (KPI Row)**:
  - Total Revenue (Sum of delivered/confirmed orders).
  - Active Orders count (Orders with status in `PENDING`, `CONFIRMED`, `PROCESSING`).
  - Total Products count (with low-stock alert pill).
  - Total Customers count.
- **Recent Orders Table**: Live-updating table with one-click status transition buttons.

### 4.3 Admin Order Fulfillment View (`/admin/orders`)
- Status tabs displaying live badge counts (`All`, `Pending (2)`, `Confirmed (5)`, `Processing (3)`, `Shipped (1)`, `Delivered (14)`).
- Quick Action Dropdown on each order row:
  - "Advance to Next Status" (computes next legal state machine step).
  - "Mark Payment Completed".
  - "Cancel & Return Stock".
