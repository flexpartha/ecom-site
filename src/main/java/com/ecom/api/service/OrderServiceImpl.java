package com.ecom.api.service;

import com.ecom.api.dto.CreateOrderRequest;
import com.ecom.api.dto.OrderItemRequest;
import com.ecom.api.dto.OrderItemResponse;
import com.ecom.api.dto.OrderResponse;
import com.ecom.api.dto.PaymentResponse;
import com.ecom.api.entity.Customer;
import com.ecom.api.entity.Order;
import com.ecom.api.entity.OrderItem;
import com.ecom.api.entity.OrderStatus;
import com.ecom.api.entity.Payment;
import com.ecom.api.entity.PaymentStatus;
import com.ecom.api.entity.Product;
import com.ecom.api.exception.BusinessRuleException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.CustomerRepository;
import com.ecom.api.repository.OrderRepository;
import com.ecom.api.repository.PaymentRepository;
import com.ecom.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED)
    );

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .build();

        // Business Rule: Stock reduces when an order is placed, not at shipment
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessRuleException("Product is not active: " + product.getName());
            }

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BusinessRuleException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity()
                        + ", requested: " + itemReq.getQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItem.calculateSubtotal();
            order.addItem(orderItem);
        }

        order.calculateTotalAmount();

        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .build();
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders(Long customerId, OrderStatus status) {
        List<Order> orders;
        if (customerId != null && status != null) {
            orders = orderRepository.findByCustomerId(customerId).stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
        } else if (customerId != null) {
            orders = orderRepository.findByCustomerId(customerId);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        OrderStatus currentStatus = order.getStatus();

        // Business Rule: Order status transitions are one-way:
        // PENDING -> CONFIRMED or CANCELLED
        // CONFIRMED -> PROCESSING or CANCELLED
        // PROCESSING -> SHIPPED or CANCELLED
        // SHIPPED -> DELIVERED
        // DELIVERED, CANCELLED, and REFUNDED are final
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException("Cannot transition order status from " + currentStatus + " to " + newStatus);
        }

        // Business Rule: Cancelling an order restores stock for all items;
        // if payment was completed, mark it REFUNDED
        if (newStatus == OrderStatus.CANCELLED) {
            applyCancellation(order);
        } else {
            order.setStatus(newStatus);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        return updateOrderStatus(id, OrderStatus.CANCELLED);
    }

    private void applyCancellation(Order order) {
        // Restore stock for all items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        // If payment was completed, mark it REFUNDED
        Payment payment = order.getPayment();
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        order.setStatus(OrderStatus.CANCELLED);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                                .productSku(item.getProduct() != null ? item.getProduct().getSku() : null)
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList();

        PaymentResponse paymentResponse = null;
        if (order.getPayment() != null) {
            Payment payment = order.getPayment();
            paymentResponse = PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(order.getId())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .amount(payment.getAmount())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }

        Customer customer = order.getCustomer();
        String customerName = customer != null ? customer.getFirstName() + " " + customer.getLastName() : null;
        String customerEmail = customer != null ? customer.getEmail() : null;
        Long customerId = customer != null ? customer.getId() : null;

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(customerId)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .items(itemResponses)
                .payment(paymentResponse)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
