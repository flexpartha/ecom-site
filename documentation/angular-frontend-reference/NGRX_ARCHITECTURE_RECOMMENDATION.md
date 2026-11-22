# Scalable NgRx State Management Architecture

## 1. Architectural Strategy & Directory Structure

To manage data flow, caching, and reactivity across the e-commerce storefront, we recommend a feature-based **NgRx Store (v16+)** architecture. 

The state is split into modular slices:
- **Core / Root State**: Router State, Auth / Customer Identity State.
- **Storefront Features**: Products, Categories, Cart, Wishlist, Orders.
- **Admin Features**: Admin Catalog Management, Admin Order Fulfillment.

```
src/app/
├── core/
│   ├── interceptors/
│   │   ├── auth.interceptor.ts
│   │   └── error.interceptor.ts
│   └── services/
│       ├── category.service.ts
│       ├── product.service.ts
│       ├── customer.service.ts
│       ├── order.service.ts
│       └── payment.service.ts
├── store/
│   ├── app.state.ts
│   ├── auth/
│   │   ├── auth.actions.ts
│   │   ├── auth.reducer.ts
│   │   ├── auth.selectors.ts
│   │   └── auth.effects.ts
│   ├── categories/
│   │   ├── category.actions.ts
│   │   ├── category.reducer.ts
│   │   ├── category.selectors.ts
│   │   └── category.effects.ts
│   ├── products/
│   │   ├── product.actions.ts
│   │   ├── product.reducer.ts
│   │   ├── product.selectors.ts
│   │   └── product.effects.ts
│   ├── cart/
│   │   ├── cart.actions.ts
│   │   ├── cart.reducer.ts
│   │   ├── cart.selectors.ts
│   │   └── cart.effects.ts
│   ├── wishlist/
│   │   ├── wishlist.actions.ts
│   │   ├── wishlist.reducer.ts
│   │   ├── wishlist.selectors.ts
│   │   └── wishlist.effects.ts
│   └── orders/
│       ├── order.actions.ts
│       ├── order.reducer.ts
│       ├── order.selectors.ts
│       └── order.effects.ts
```

---

## 2. Global Root State Interface (`app.state.ts`)

```typescript
import { AuthState } from './auth/auth.reducer';
import { CategoryState } from './categories/category.reducer';
import { ProductState } from './products/product.reducer';
import { CartState } from './cart/cart.reducer';
import { WishlistState } from './wishlist/wishlist.reducer';
import { OrderState } from './orders/order.reducer';

export interface AppState {
  auth: AuthState;
  categories: CategoryState;
  products: ProductState;
  cart: CartState;
  wishlist: WishlistState;
  orders: OrderState;
}
```

---

## 3. Product Catalog Feature Slice (Using `@ngrx/entity`)

Manages the product catalog, caching, filtering, and single-product inspection.

### 3.1 State Interface & Entity Adapter (`product.reducer.ts`)

```typescript
import { createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { ProductResponse } from '../../core/models/product.model';
import * as ProductActions from './product.actions';

export interface ProductState extends EntityState<ProductResponse> {
  selectedProductId: number | null;
  loading: boolean;
  error: string | null;
  selectedCategoryFilter: number | null;
  searchQuery: string;
  priceFilter: { min: number | null; max: number | null };
}

export const productAdapter: EntityAdapter<ProductResponse> = createEntityAdapter<ProductResponse>({
  selectId: (product: ProductResponse) => product.id,
  sortComparer: (a, b) => a.name.localeCompare(b.name)
});

export const initialProductState: ProductState = productAdapter.getInitialState({
  selectedProductId: null,
  loading: false,
  error: null,
  selectedCategoryFilter: null,
  searchQuery: '',
  priceFilter: { min: null, max: null }
});

export const productReducer = createReducer(
  initialProductState,
  on(ProductActions.loadProducts, (state) => ({ ...state, loading: true, error: null })),
  on(ProductActions.loadProductsSuccess, (state, { products }) =>
    productAdapter.setAll(products, { ...state, loading: false })
  ),
  on(ProductActions.loadProductsFailure, (state, { error }) => ({ ...state, loading: false, error })),
  
  on(ProductActions.loadProductById, (state) => ({ ...state, loading: true })),
  on(ProductActions.loadProductByIdSuccess, (state, { product }) =>
    productAdapter.upsertOne(product, { ...state, selectedProductId: product.id, loading: false })
  ),
  
  on(ProductActions.setCategoryFilter, (state, { categoryId }) => ({ ...state, selectedCategoryFilter: categoryId })),
  on(ProductActions.setSearchQuery, (state, { query }) => ({ ...state, searchQuery: query })),
  on(ProductActions.setPriceFilter, (state, { min, max }) => ({ ...state, priceFilter: { min, max } }))
);
```

### 3.2 Actions (`product.actions.ts`)

```typescript
import { createAction, props } from '@ngrx/store';
import { ProductResponse, ProductRequest } from '../../core/models/product.model';

export const loadProducts = createAction(
  '[Product List] Load Products',
  props<{ categoryId?: number; active?: boolean }>()
);

export const loadProductsSuccess = createAction(
  '[Product API] Load Products Success',
  props<{ products: ProductResponse[] }>()
);

export const loadProductsFailure = createAction(
  '[Product API] Load Products Failure',
  props<{ error: string }>()
);

export const loadProductById = createAction(
  '[Product Detail] Load Product By Id',
  props<{ id: number }>()
);

export const loadProductByIdSuccess = createAction(
  '[Product API] Load Product By Id Success',
  props<{ product: ProductResponse }>()
);

export const setCategoryFilter = createAction(
  '[Product Filter] Set Category',
  props<{ categoryId: number | null }>()
);

export const setSearchQuery = createAction(
  '[Product Filter] Set Search Query',
  props<{ query: string }>()
);

export const setPriceFilter = createAction(
  '[Product Filter] Set Price Filter',
  props<{ min: number | null; max: number | null }>()
);
```

### 3.3 Effects (`product.effects.ts`)

```typescript
import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ProductService } from '../../core/services/product.service';
import * as ProductActions from './product.actions';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';

@Injectable()
export class ProductEffects {

  loadProducts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.loadProducts),
      mergeMap(({ categoryId, active }) =>
        this.productService.getAllProducts(categoryId, active).pipe(
          map((products) => ProductActions.loadProductsSuccess({ products })),
          catchError((err) => of(ProductActions.loadProductsFailure({ error: err.message })))
        )
      )
    )
  );

  loadProductById$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.loadProductById),
      mergeMap(({ id }) =>
        this.productService.getProductById(id).pipe(
          map((product) => ProductActions.loadProductByIdSuccess({ product })),
          catchError((err) => of(ProductActions.loadProductsFailure({ error: err.message })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private productService: ProductService
  ) {}
}
```

### 3.4 Selectors (`product.selectors.ts`)

```typescript
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ProductState, productAdapter } from './product.reducer';

export const selectProductState = createFeatureSelector<ProductState>('products');

const { selectAll, selectEntities } = productAdapter.getSelectors();

export const selectAllProducts = createSelector(selectProductState, selectAll);
export const selectProductEntities = createSelector(selectProductState, selectEntities);
export const selectProductsLoading = createSelector(selectProductState, (state) => state.loading);
export const selectSelectedProductId = createSelector(selectProductState, (state) => state.selectedProductId);

export const selectCurrentProduct = createSelector(
  selectProductEntities,
  selectSelectedProductId,
  (entities, id) => (id ? entities[id] ?? null : null)
);

// High-Performance Filtered Products Selector
export const selectFilteredProducts = createSelector(
  selectAllProducts,
  selectProductState,
  (products, state) => {
    return products.filter((product) => {
      // 1. Category Filter
      if (state.selectedCategoryFilter && product.categoryId !== state.selectedCategoryFilter) {
        return false;
      }
      // 2. Search Query (Title or Description)
      if (state.searchQuery) {
        const query = state.searchQuery.toLowerCase();
        const matchName = product.name.toLowerCase().includes(query);
        const matchDesc = product.description?.toLowerCase().includes(query);
        if (!matchName && !matchDesc) return false;
      }
      // 3. Price Filter
      if (state.priceFilter.min !== null && product.price < state.priceFilter.min) {
        return false;
      }
      if (state.priceFilter.max !== null && product.price > state.priceFilter.max) {
        return false;
      }
      return true;
    });
  }
);
```

---

## 4. Shopping Cart Feature Slice (Client-Side with Persistence)

Since the backend lacks a cart endpoint, this slice manages cart state, quantity bounds, subtotal calculations, and automatically synchronizes to `localStorage`.

### 4.1 State Interface & Reducer (`cart.reducer.ts`)

```typescript
import { createReducer, on } from '@ngrx/store';
import { ProductResponse } from '../../core/models/product.model';
import * as CartActions from './cart.actions';

export interface CartItem {
  product: ProductResponse;
  quantity: number;
  subtotal: number;
}

export interface CartState {
  items: CartItem[];
  totalQuantity: number;
  totalAmount: number;
}

// Load initial cart from LocalStorage
const savedCart = localStorage.getItem('ecom_cart_session');
export const initialCartState: CartState = savedCart
  ? JSON.parse(savedCart)
  : { items: [], totalQuantity: 0, totalAmount: 0 };

function recalculateCart(items: CartItem[]): { totalQuantity: number; totalAmount: number } {
  const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalAmount = items.reduce((sum, item) => sum + item.subtotal, 0);
  return {
    totalQuantity,
    totalAmount: Math.round(totalAmount * 100) / 100
  };
}

export const cartReducer = createReducer(
  initialCartState,
  
  on(CartActions.addToCart, (state, { product, quantity }) => {
    const existingIndex = state.items.findIndex((item) => item.product.id === product.id);
    let updatedItems: CartItem[];

    if (existingIndex > -1) {
      const existing = state.items[existingIndex];
      const newQty = Math.min(existing.quantity + quantity, product.stockQuantity);
      const updatedItem: CartItem = {
        ...existing,
        quantity: newQty,
        subtotal: Math.round(newQty * product.price * 100) / 100
      };
      updatedItems = [...state.items];
      updatedItems[existingIndex] = updatedItem;
    } else {
      const initialQty = Math.min(quantity, product.stockQuantity);
      updatedItems = [
        ...state.items,
        {
          product,
          quantity: initialQty,
          subtotal: Math.round(initialQty * product.price * 100) / 100
        }
      ];
    }

    const { totalQuantity, totalAmount } = recalculateCart(updatedItems);
    return { ...state, items: updatedItems, totalQuantity, totalAmount };
  }),

  on(CartActions.updateQuantity, (state, { productId, quantity }) => {
    if (quantity <= 0) {
      const filtered = state.items.filter((item) => item.product.id !== productId);
      const { totalQuantity, totalAmount } = recalculateCart(filtered);
      return { ...state, items: filtered, totalQuantity, totalAmount };
    }

    const updatedItems = state.items.map((item) => {
      if (item.product.id === productId) {
        const boundedQty = Math.min(quantity, item.product.stockQuantity);
        return {
          ...item,
          quantity: boundedQty,
          subtotal: Math.round(boundedQty * item.product.price * 100) / 100
        };
      }
      return item;
    });

    const { totalQuantity, totalAmount } = recalculateCart(updatedItems);
    return { ...state, items: updatedItems, totalQuantity, totalAmount };
  }),

  on(CartActions.removeFromCart, (state, { productId }) => {
    const updatedItems = state.items.filter((item) => item.product.id !== productId);
    const { totalQuantity, totalAmount } = recalculateCart(updatedItems);
    return { ...state, items: updatedItems, totalQuantity, totalAmount };
  }),

  on(CartActions.clearCart, () => ({ items: [], totalQuantity: 0, totalAmount: 0 }))
);
```

### 4.2 Storage Synchronization Effect (`cart.effects.ts`)

```typescript
import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import * as CartActions from './cart.actions';
import { selectCartState } from './cart.selectors';
import { tap, withLatestFrom } from 'rxjs/operators';
import { AppState } from '../app.state';

@Injectable()
export class CartEffects {

  syncCartToLocalStorage$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(
          CartActions.addToCart,
          CartActions.updateQuantity,
          CartActions.removeFromCart,
          CartActions.clearCart
        ),
        withLatestFrom(this.store.select(selectCartState)),
        tap(([_, cartState]) => {
          localStorage.setItem('ecom_cart_session', JSON.stringify(cartState));
        })
      ),
    { dispatch: false }
  );

  constructor(
    private actions$: Actions,
    private store: Store<AppState>
  ) {}
}
```

---

## 5. Orders & Checkout Feature Slice

Manages order placement, order status lifecycle, and order history caching.

### 5.1 State Interface (`order.reducer.ts`)

```typescript
import { createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { OrderResponse } from '../../core/models/order.model';
import * as OrderActions from './order.actions';

export interface OrderState extends EntityState<OrderResponse> {
  activeOrder: OrderResponse | null;
  loading: boolean;
  submittingOrder: boolean;
  error: string | null;
}

export const orderAdapter: EntityAdapter<OrderResponse> = createEntityAdapter<OrderResponse>({
  selectId: (order: OrderResponse) => order.id,
  sortComparer: (a, b) => b.id - a.id // Newest orders first
});

export const initialOrderState: OrderState = orderAdapter.getInitialState({
  activeOrder: null,
  loading: false,
  submittingOrder: false,
  error: null
});

export const orderReducer = createReducer(
  initialOrderState,

  // Submit Checkout Order
  on(OrderActions.createOrder, (state) => ({ ...state, submittingOrder: true, error: null })),
  on(OrderActions.createOrderSuccess, (state, { order }) =>
    orderAdapter.addOne(order, { ...state, activeOrder: order, submittingOrder: false })
  ),
  on(OrderActions.createOrderFailure, (state, { error }) => ({ ...state, submittingOrder: false, error })),

  // Load Order History
  on(OrderActions.loadCustomerOrders, (state) => ({ ...state, loading: true })),
  on(OrderActions.loadCustomerOrdersSuccess, (state, { orders }) =>
    orderAdapter.setAll(orders, { ...state, loading: false })
  ),

  // Cancel Order
  on(OrderActions.cancelOrderSuccess, (state, { order }) =>
    orderAdapter.updateOne({ id: order.id, changes: order }, state)
  )
);
```

### 5.2 Order Placement Effect (`order.effects.ts`)

```typescript
import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { OrderService } from '../../core/services/order.service';
import * as OrderActions from './order.actions';
import * as CartActions from '../cart/cart.actions';
import { of } from 'rxjs';
import { map, mergeMap, catchError, switchMap, tap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class OrderEffects {

  createOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.createOrder),
      mergeMap(({ request }) =>
        this.orderService.createOrder(request).pipe(
          switchMap((order) => [
            OrderActions.createOrderSuccess({ order }),
            CartActions.clearCart() // Clear cart upon successful order
          ]),
          tap(({ order }) => {
            this.router.navigate(['/orders', order.orderNumber, 'confirmed']);
          }),
          catchError((err) => of(OrderActions.createOrderFailure({ error: err.error?.message || err.message })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private orderService: OrderService,
    private router: Router
  ) {}
}
```
