package com.ecom.api.controller;

import com.ecom.api.dto.CreateOrderRequest;
import com.ecom.api.dto.OrderItemRequest;
import com.ecom.api.dto.OrderItemResponse;
import com.ecom.api.dto.OrderResponse;
import com.ecom.api.dto.PaymentResponse;
//import com.ecom.api.dto.UpdateOrderStatusRequest;
import com.ecom.api.entity.OrderStatus;
import com.ecom.api.entity.PaymentMethod;
import com.ecom.api.entity.PaymentStatus;
import com.ecom.api.exception.BusinessRuleException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void should_return201Created_when_createOrder() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Maple St")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(2).build()))
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.98"))
                .shippingAddress("123 Maple St")
                .items(List.of(OrderItemResponse.builder()
                        .id(1L)
                        .productId(1L)
                        .productName("Wireless Mouse")
                        .quantity(2)
                        .unitPrice(new BigDecimal("24.99"))
                        .subtotal(new BigDecimal("49.98"))
                        .build()))
                .payment(PaymentResponse.builder()
                        .id(1L)
                        .orderId(1L)
                        .paymentMethod(PaymentMethod.CREDIT_CARD)
                        .paymentStatus(PaymentStatus.PENDING)
                        .amount(new BigDecimal("49.98"))
                        .build())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345678"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(49.98));
    }

    @Test
    void should_return422UnprocessableEntity_when_businessRuleViolated() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Maple St")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(200).build()))
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new BusinessRuleException("Insufficient stock for product"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Insufficient stock for product"));
    }

    @Test
    void should_return404NotFound_when_orderNotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"));
    }

    @Test
    void should_return200Ok_when_orderCancelled() throws Exception {
        // Arrange
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderService.cancelOrder(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void should_return400BadRequest_when_invalidRequestPayload() throws Exception {
        // Arrange: Missing items and shippingAddress
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isMap());
    }
}
