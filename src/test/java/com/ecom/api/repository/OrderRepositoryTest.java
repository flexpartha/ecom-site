package com.ecom.api.repository;

import com.ecom.api.entity.Customer;
import com.ecom.api.entity.Order;
import com.ecom.api.entity.OrderItem;
import com.ecom.api.entity.OrderStatus;
import com.ecom.api.entity.Payment;
import com.ecom.api.entity.PaymentMethod;
import com.ecom.api.entity.PaymentStatus;
import com.ecom.api.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void should_cascadeSaveItemsAndPayment_when_orderSaved() {
        // Arrange
        Customer customer = customerRepository.findAll().get(0);
        Product product = productRepository.findAll().get(0);

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Test Street")
                .totalAmount(new BigDecimal("49.98"))
                .build();

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("24.99"))
                .build();
        order.addItem(item);

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("49.98"))
                .build();
        order.setPayment(payment);

        // Act
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();

        // Assert
        assertThat(savedOrder.getId()).isNotNull();
    }

    @Test
    void should_generateOrderNumber_when_orderSaved() {
        // Arrange
        Customer customer = customerRepository.findAll().get(0);

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .shippingAddress("456 Avenue")
                .totalAmount(BigDecimal.TEN)
                .build();

        // Act
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();

        // Assert
        assertThat(savedOrder.getOrderNumber())
                .isNotNull()
                .startsWith("ORD-")
                .hasSize(12)
                .matches("^ORD-[0-9A-F]{8}$");
    }

    @Test
    void should_calculateSubtotalAutomatically_when_orderSavedWithItems() {
        // Arrange
        Customer customer = customerRepository.findAll().get(0);
        Product product = productRepository.findAll().get(0);

        Order order = Order.builder()
                .customer(customer)
                .shippingAddress("789 Boulevard")
                .build();

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(3)
                .unitPrice(new BigDecimal("20.00"))
                .build();
        order.addItem(item);

        // Act
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();
        OrderItem savedItem = orderItemRepository.findByOrderId(savedOrder.getId()).get(0);

        // Assert
        assertThat(savedItem.getSubtotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void should_cascadePersistPayment_when_orderSavedWithPayment() {
        // Arrange
        Customer customer = customerRepository.findAll().get(0);

        Order order = Order.builder()
                .customer(customer)
                .shippingAddress("101 Pine Road")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.UPI)
                .amount(new BigDecimal("100.00"))
                .build();
        order.setPayment(payment);

        // Act
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();

        // Assert
        assertThat(paymentRepository.findByOrderId(savedOrder.getId())).isPresent();
    }
}
