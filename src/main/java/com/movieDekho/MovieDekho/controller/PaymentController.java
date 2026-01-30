package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.service.paymentService.RazorpayPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Razorpay Payment Controller
 * Handles payment initiation, verification, and webhook processing
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Razorpay payment gateway integration for ticket bookings")
public class PaymentController {

    private final RazorpayPaymentService razorpayPaymentService;
    private final JwtUtils jwtUtils;

    /**
     * Initiate payment - Create Razorpay order
     * Call this endpoint to start payment flow
     */
    @PostMapping("/initiate")
    @Operation(
        summary = "Initiate Payment (Auth Required)",
        description = "Create a Razorpay order for payment. Frontend will use the order details to process payment on client side.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payment initiation details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiateRequest.class),
                examples = @ExampleObject(value = """
                    {
                        "slotId": 1,
                        "seatNumbers": ["A1", "A2", "A3"],
                        "totalAmount": 450.00,
                        "phoneNumber": "9876543210"
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order created successfully", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PaymentInitiateResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Payment gateway error")
    })
    public ResponseEntity<?> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing authentication token");
            }

            // Create a unique booking reference ID
            String bookingReference = "BOOKING_" + System.currentTimeMillis();

            // Create Razorpay order
            RazorpayPaymentService.RazorpayOrderResponse order = 
                razorpayPaymentService.createOrder(
                    request.getTotalAmount(),
                    bookingReference,
                    userEmail,
                    request.getPhoneNumber()
                );

            // Return order details to frontend
            PaymentInitiateResponse response = new PaymentInitiateResponse();
            response.setOrderId(order.getOrderId());
            response.setAmount(order.getAmount());
            response.setKeyId(order.getKeyId());
            response.setBookingReference(bookingReference);
            response.setUserEmail(userEmail);
            response.setMessage("Order created successfully. Proceed to payment on frontend.");

            log.info("Payment initiated successfully: {}", bookingReference);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error initiating payment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to initiate payment: " + e.getMessage()));
        }
    }

    /**
     * Verify payment - Called after frontend completes payment
     * This confirms the payment with Razorpay
     */
    @PostMapping("/verify")
    @Operation(
        summary = "Verify Payment (Auth Required)",
        description = "Verify payment signature after frontend payment completion. Use this to confirm payment before creating booking.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payment verification details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentVerifyRequest.class),
                examples = @ExampleObject(value = """
                    {
                        "orderId": "order_1234567890",
                        "paymentId": "pay_1234567890",
                        "signature": "signature_hash"
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
        @ApiResponse(responseCode = "400", description = "Payment verification failed"),
        @ApiResponse(responseCode = "500", description = "Verification error")
    })
    public ResponseEntity<?> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing authentication token");
            }

            // Verify payment signature
            boolean isValid = razorpayPaymentService.verifyPaymentSignature(
                    request.getOrderId(),
                    request.getPaymentId(),
                    request.getSignature()
            );

            if (!isValid) {
                log.warn("Payment signature verification failed for user: {}", userEmail);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Payment verification failed. Invalid signature."));
            }

            // Fetch payment details from Razorpay
            RazorpayPaymentService.PaymentDetailsResponse paymentDetails = 
                razorpayPaymentService.getPaymentDetails(request.getPaymentId());

            PaymentVerifyResponse response = new PaymentVerifyResponse();
            response.setStatus("SUCCESS");
            response.setPaymentId(request.getPaymentId());
            response.setOrderId(request.getOrderId());
            response.setAmount(paymentDetails.getAmount());
            response.setMethod(paymentDetails.getMethod());
            response.setMessage("Payment verified successfully. You can now create booking.");

            log.info("Payment verified successfully for user: {} - Order: {}", userEmail, request.getOrderId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify payment: " + e.getMessage()));
        }
    }

    /**
     * Get payment details
     * Fetch details of a completed payment
     */
    @GetMapping("/{paymentId}/details")
    @Operation(
        summary = "Get Payment Details (Auth Required)",
        description = "Fetch details of a payment from Razorpay",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Error fetching payment details")
    })
    public ResponseEntity<?> getPaymentDetails(
            @Parameter(description = "Razorpay Payment ID", required = true)
            @PathVariable String paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing authentication token");
            }

            RazorpayPaymentService.PaymentDetailsResponse details = 
                razorpayPaymentService.getPaymentDetails(paymentId);

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            log.error("Error fetching payment details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch payment details: " + e.getMessage()));
        }
    }

    /**
     * Process refund
     */
    @PostMapping("/{paymentId}/refund")
    @Operation(
        summary = "Process Refund (Admin Only)",
        description = "Process refund for a payment",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "500", description = "Refund processing error")
    })
    public ResponseEntity<?> processRefund(
            @Parameter(description = "Payment ID to refund", required = true)
            @PathVariable String paymentId,
            @RequestParam(value = "amount", required = false) Double refundAmount,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing authentication token");
            }

            // Fetch payment details to get amount
            RazorpayPaymentService.PaymentDetailsResponse paymentDetails = 
                razorpayPaymentService.getPaymentDetails(paymentId);

            double amountToRefund = refundAmount != null ? refundAmount : paymentDetails.getAmount();

            // Process refund
            RazorpayPaymentService.RefundResponse refund = 
                razorpayPaymentService.refundPayment(paymentId, amountToRefund);

            log.info("Refund processed successfully - Payment: {}, Refund: {}", 
                    paymentId, refund.getRefundId());

            return ResponseEntity.ok(refund);

        } catch (Exception e) {
            log.error("Error processing refund: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process refund: " + e.getMessage()));
        }
    }

    /**
     * Razorpay Webhook endpoint (Optional)
     * Razorpay sends payment status updates here
     */
    @PostMapping("/webhook/razorpay")
    @Operation(
        summary = "Razorpay Webhook",
        description = "Webhook endpoint for Razorpay payment status updates (Optional)"
    )
    public ResponseEntity<?> handleRazorpayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received Razorpay webhook: {}", payload);
            return ResponseEntity.ok(Map.of("status", "received"));

        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Webhook processing failed"));
        }
    }

    /**
     * Extract user email from JWT token
     */
    private String extractUserEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtils.getNameFromJwt(token);
    }

    // ========================== Request/Response DTOs ==========================

    public static class PaymentInitiateRequest {
        private Long slotId;
        private java.util.List<String> seatNumbers;
        private double totalAmount;
        private String phoneNumber;

        // Getters and Setters
        public Long getSlotId() { return slotId; }
        public void setSlotId(Long slotId) { this.slotId = slotId; }

        public java.util.List<String> getSeatNumbers() { return seatNumbers; }
        public void setSeatNumbers(java.util.List<String> seatNumbers) { this.seatNumbers = seatNumbers; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    public static class PaymentInitiateResponse {
        private String orderId;
        private double amount;
        private String keyId;
        private String bookingReference;
        private String userEmail;
        private String message;

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getKeyId() { return keyId; }
        public void setKeyId(String keyId) { this.keyId = keyId; }

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PaymentVerifyRequest {
        private String orderId;
        private String paymentId;
        private String signature;

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
    }

    public static class PaymentVerifyResponse {
        private String status;
        private String paymentId;
        private String orderId;
        private double amount;
        private String method;
        private String message;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
