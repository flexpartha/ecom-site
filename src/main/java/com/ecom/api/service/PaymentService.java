package com.ecom.api.service;

import com.ecom.api.dto.PaymentResponse;
import com.ecom.api.entity.PaymentStatus;

public interface PaymentService {

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse updatePaymentStatus(Long orderId, PaymentStatus status);
}
