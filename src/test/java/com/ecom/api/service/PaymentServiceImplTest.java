package com.ecom.api.service;

import com.ecom.api.dto.PaymentResponse;
import com.ecom.api.entity.Order;
import com.ecom.api.entity.Payment;
import com.ecom.api.entity.PaymentMethod;
import com.ecom.api.entity.PaymentStatus;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void should_returnPayment_when_paymentFoundByOrderId() {
        // Arrange
        Order order = Order.builder().id(100L).build();
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPaymentByOrderId(100L);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(1L);
                    assertThat(res.getOrderId()).isEqualTo(100L);
                    assertThat(res.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
                    assertThat(res.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
                    assertThat(res.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_paymentNotFoundByOrderId() {
        // Arrange
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found for order id: 999");
    }

    @Test
    void should_updatePaymentStatus_when_paymentFound() {
        // Arrange
        Order order = Order.builder().id(100L).build();
        Payment existingPayment = Payment.builder()
                .id(1L)
                .order(order)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("150.00"))
                .build();

        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.updatePaymentStatus(100L, PaymentStatus.COMPLETED);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
                    assertThat(res.getOrderId()).isEqualTo(100L);
                    assertThat(res.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_updatePaymentStatusNotFound() {
        // Arrange
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.updatePaymentStatus(999L, PaymentStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found for order id: 999");
    }
}
