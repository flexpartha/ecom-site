package com.ecom.api.controller;

import com.ecom.api.dto.PaymentResponse;
import com.ecom.api.dto.UpdatePaymentStatusRequest;
import com.ecom.api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get payment by order ID", description = "Retrieve payment details for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payment",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found for the order",
                    content = @Content)
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "ID of the order to retrieve payment for") @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @Operation(summary = "Update payment status", description = "Update the payment status for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Payment not found for the order",
                    content = @Content)
    })
    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "ID of the order to update payment for") @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(orderId, request.getPaymentStatus()));
    }
}
