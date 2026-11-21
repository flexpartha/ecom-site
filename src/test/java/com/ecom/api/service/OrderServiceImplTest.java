package com.ecom.api.service;

import com.ecom.api.dto.CreateOrderRequest;
import com.ecom.api.dto.OrderItemRequest;
import com.ecom.api.dto.OrderResponse;
import com.ecom.api.entity.Customer;
import com.ecom.api.entity.Order;
import com.ecom.api.entity.OrderItem;
import com.ecom.api.entity.OrderStatus;
import com.ecom.api.entity.Payment;
import com.ecom.api.entity.PaymentMethod;
import com.ecom.api.entity.PaymentStatus;
import com.ecom.api.entity.Product;
import com.ecom.api.exception.BusinessRuleException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.CustomerRepository;
import com.ecom.api.repository.OrderRepository;
import com.ecom.api.repository.PaymentRepository;
import com.ecom.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // --- Business Rule 1: Stock reduces when an order is placed, not at shipment ---

    @Test
    void should_throwResourceNotFoundException_when_customerNotFoundOnCreateOrder() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(999L)
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(1).build()))
                .build();

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");
    }

    @Test
    void should_throwResourceNotFoundException_when_productNotFoundOnCreateOrder() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(999L).quantity(1).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void should_reduceStockAndCreateOrder_when_sufficientStock() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build();
        Product product = Product.builder()
                .id(10L)
                .name("Wireless Mouse")
                .price(new BigDecimal("25.00"))
                .sku("MOU-001")
                .stockQuantity(100)
                .active(true)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(5).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            o.setOrderNumber("ORD-12345678");
            return o;
        });

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertThat(product.getStockQuantity()).isEqualTo(95);
    }

    @Test
    void should_throwBusinessRuleException_when_insufficientStock() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").build();
        Product product = Product.builder()
                .id(10L)
                .name("Wireless Mouse")
                .price(new BigDecimal("25.00"))
                .stockQuantity(3)
                .active(true)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(5).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void should_throwBusinessRuleException_when_productInactive() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").build();
        Product product = Product.builder()
                .id(10L)
                .name("Inactive Item")
                .stockQuantity(50)
                .active(false)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(2).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not active");
    }

    // --- Business Rule 2: Cancelling an order restores stock for all items; if payment completed, mark REFUNDED ---

    @Test
    void should_restoreStockAndSetCancelled_when_orderCancelled() {
        // Arrange
        Product product = Product.builder().id(10L).stockQuantity(45).build();
        OrderItem item = OrderItem.builder().id(1L).product(product).quantity(5).unitPrice(new BigDecimal("20.00")).build();
        Payment payment = Payment.builder().paymentStatus(PaymentStatus.PENDING).build();
        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(List.of(item)))
                .payment(payment)
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.cancelOrder(100L);

        // Assert
        assertThat(product.getStockQuantity()).isEqualTo(50);
    }

    @Test
    void should_refundPayment_when_orderCancelledAndPaymentCompleted() {
        // Arrange
        Product product = Product.builder().id(10L).stockQuantity(45).build();
        OrderItem item = OrderItem.builder().id(1L).product(product).quantity(5).unitPrice(new BigDecimal("20.00")).build();
        Payment payment = Payment.builder().id(50L).paymentStatus(PaymentStatus.COMPLETED).build();
        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.CONFIRMED)
                .items(new ArrayList<>(List.of(item)))
                .payment(payment)
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.cancelOrder(100L);

        // Assert
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void should_notRefundPayment_when_orderCancelledAndPaymentPending() {
        // Arrange
        Product product = Product.builder().id(10L).stockQuantity(10).build();
        OrderItem item = OrderItem.builder().id(1L).product(product).quantity(2).unitPrice(new BigDecimal("10.00")).build();
        Payment payment = Payment.builder().id(50L).paymentStatus(PaymentStatus.PENDING).build();
        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(List.of(item)))
                .payment(payment)
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.cancelOrder(100L);

        // Assert
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // --- Business Rule 3: Order status transitions are one-way ---

    @Test
    void should_transitionToConfirmed_when_statusPending() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void should_transitionToProcessing_when_statusConfirmed() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.updateOrderStatus(100L, OrderStatus.PROCESSING);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void should_transitionToShipped_when_statusProcessing() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.PROCESSING).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.updateOrderStatus(100L, OrderStatus.SHIPPED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void should_transitionToDelivered_when_statusShipped() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.SHIPPED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.updateOrderStatus(100L, OrderStatus.DELIVERED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void should_throwBusinessRuleException_when_transitionFromDelivered() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(100L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot transition order status from DELIVERED to CANCELLED");
    }

    @Test
    void should_throwBusinessRuleException_when_transitionFromCancelled() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot transition order status from CANCELLED to CONFIRMED");
    }

    @Test
    void should_throwBusinessRuleException_when_invalidTransitionAttempted() {
        // Arrange: PENDING cannot jump directly to SHIPPED
        Order order = Order.builder().id(100L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(100L, OrderStatus.SHIPPED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot transition order status from PENDING to SHIPPED");
    }

    @Test
    void should_throwBusinessRuleException_when_cancelOrderAlreadyDelivered() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot transition order status from DELIVERED to CANCELLED");
    }

    @Test
    void should_throwBusinessRuleException_when_cancelOrderAlreadyCancelled() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot transition order status from CANCELLED to CANCELLED");
    }

    @Test
    void should_throwResourceNotFoundException_when_cancelOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: 999");
    }

    @Test
    void should_throwResourceNotFoundException_when_updateOrderStatusOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: 999");
    }

    @Test
    void should_returnOrder_when_orderFoundById() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build();
        Order order = Order.builder()
                .id(100L)
                .orderNumber("ORD-12345678")
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrderById(100L);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(100L);
                    assertThat(res.getOrderNumber()).isEqualTo("ORD-12345678");
                    assertThat(res.getCustomerId()).isEqualTo(1L);
                    assertThat(res.getStatus()).isEqualTo(OrderStatus.PENDING);
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_orderNotFoundById() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: 999");
    }

    @Test
    void should_returnOrder_when_orderFoundByOrderNumber() {
        // Arrange
        Order order = Order.builder().id(100L).orderNumber("ORD-12345678").build();
        when(orderRepository.findByOrderNumber("ORD-12345678")).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrderByOrderNumber("ORD-12345678");

        // Assert
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void should_throwResourceNotFoundException_when_orderNotFoundByOrderNumber() {
        // Arrange
        when(orderRepository.findByOrderNumber("NON-EXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderByOrderNumber("NON-EXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with order number: NON-EXISTENT");
    }

    @Test
    void should_returnOrders_when_filteredByCustomerIdAndStatus() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.PENDING).build();
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders(1L, OrderStatus.PENDING);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .first()
                .satisfies(res -> assertThat(res.getId()).isEqualTo(100L));
    }

    @Test
    void should_returnOrders_when_filteredByCustomerIdOnly() {
        // Arrange
        Order order = Order.builder().id(100L).build();
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders(1L, null);

        // Assert
        assertThat(responses).hasSize(1);
    }

    @Test
    void should_returnOrders_when_filteredByStatusOnly() {
        // Arrange
        Order order = Order.builder().id(100L).status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findByStatus(OrderStatus.CONFIRMED)).thenReturn(List.of(order));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders(null, OrderStatus.CONFIRMED);

        // Assert
        assertThat(responses).hasSize(1);
    }

    @Test
    void should_returnAllOrders_when_noFiltersProvided() {
        // Arrange
        Order order = Order.builder().id(100L).build();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders(null, null);

        // Assert
        assertThat(responses).hasSize(1);
    }
}
