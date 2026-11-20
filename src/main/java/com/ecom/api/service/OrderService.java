package com.ecom.api.service;

import com.ecom.api.dto.CreateOrderRequest;
import com.ecom.api.dto.OrderResponse;
import com.ecom.api.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getAllOrders(Long customerId, OrderStatus status);

    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

    OrderResponse cancelOrder(Long id);
}
