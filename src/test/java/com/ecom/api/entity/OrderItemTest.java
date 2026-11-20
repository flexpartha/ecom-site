package com.ecom.api.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    void should_calculateSubtotal_when_unitPriceAndQuantitySet() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .unitPrice(new BigDecimal("24.99"))
                .quantity(3)
                .build();

        // Act
        item.calculateSubtotal();

        // Assert
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("74.97"));
    }

    @Test
    void should_automaticallyCalculateSubtotal_when_getSubtotalCalled() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .unitPrice(new BigDecimal("15.50"))
                .quantity(2)
                .build();

        // Act
        BigDecimal subtotal = item.getSubtotal();

        // Assert
        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("31.00"));
    }

    @Test
    void should_recalculateSubtotal_when_quantityChangedViaSetter() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .unitPrice(new BigDecimal("10.00"))
                .quantity(2)
                .build();

        // Act
        item.setQuantity(5);

        // Assert
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void should_recalculateSubtotal_when_unitPriceChangedViaSetter() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .unitPrice(new BigDecimal("10.00"))
                .quantity(3)
                .build();

        // Act
        item.setUnitPrice(new BigDecimal("20.00"));

        // Assert
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("60.00"));
    }
}
