package com.ecom.api.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void should_generateOrderNumber_when_persistedWithoutOrderNumber() {
        // Arrange
        Order order = Order.builder()
                .shippingAddress("123 Test St")
                .build();

        // Act
        order.onCreate();

        // Assert
        assertThat(order.getOrderNumber())
                .isNotNull()
                .startsWith("ORD-")
                .hasSize(12)
                .matches("^ORD-[0-9A-F]{8}$");
    }

    @Test
    void should_retainExistingOrderNumber_when_orderNumberAlreadySet() {
        // Arrange
        String existingOrderNumber = "ORD-ABC12345";
        Order order = Order.builder()
                .orderNumber(existingOrderNumber)
                .shippingAddress("123 Test St")
                .build();

        // Act
        order.onCreate();

        // Assert
        assertThat(order.getOrderNumber()).isEqualTo(existingOrderNumber);
    }

    @Test
    void should_linkBidirectionally_when_itemAddedToOrder() {
        // Arrange
        Order order = Order.builder().build();
        OrderItem item = OrderItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .build();

        // Act
        order.addItem(item);

        // Assert
        assertThat(order.getItems()).contains(item);
    }

    @Test
    void should_setOrderReference_when_itemAddedToOrder() {
        // Arrange
        Order order = Order.builder().build();
        OrderItem item = OrderItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .build();

        // Act
        order.addItem(item);

        // Assert
        assertThat(item.getOrder()).isSameAs(order);
    }

    @Test
    void should_linkBidirectionally_when_paymentSetOnOrder() {
        // Arrange
        Order order = Order.builder().build();
        Payment payment = Payment.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // Act
        order.setPayment(payment);

        // Assert
        assertThat(payment.getOrder()).isSameAs(order);
    }

    @Test
    void should_calculateTotalAmount_when_itemsPresent() {
        // Arrange
        Order order = Order.builder().build();
        OrderItem item1 = OrderItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .build();
        OrderItem item2 = OrderItem.builder()
                .quantity(1)
                .unitPrice(new BigDecimal("25.50"))
                .build();
        order.addItem(item1);
        order.addItem(item2);

        // Act
        order.calculateTotalAmount();

        // Assert
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45.50"));
    }
}
