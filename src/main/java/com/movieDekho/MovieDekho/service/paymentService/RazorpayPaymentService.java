package com.movieDekho.MovieDekho.service.paymentService;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayPaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            log.info("RazorpayClient initialized successfully.");
            boolean isTestMode = razorpayKeyId != null && razorpayKeyId.startsWith("rzp_test_");
            if (isTestMode) {
                log.warn("🧪 RAZORPAY TEST MODE ACTIVE - No real money will be charged!");
                log.info("Using test key: {}***", razorpayKeyId.substring(0, 12));
            } else {
                log.error("💰 RAZORPAY LIVE MODE ACTIVE - REAL MONEY WILL BE CHARGED!");
                log.warn("Using live key: {}*** - ENSURE THIS IS INTENDED FOR PRODUCTION!",
                        razorpayKeyId.substring(0, 12));
            }
        } catch (RazorpayException e) {
            log.error("Failed to initialize RazorpayClient", e);
            // Depending on the application's requirements, you might want to
            // prevent the application from starting if the payment client fails to initialize.
            // For now, we'll log the error and let the application start.
            // throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }
    public RazorpayOrderResponse createOrder(double amount, String orderId,
            String customerEmail, String customerPhone) {
        try {
            log.info("Creating Razorpay order for amount: {} paise, orderId: {}",
                    (long) (amount * 100), orderId);

            // Amount should be in paise (multiply by 100)
            long amountInPaise = (long) (amount * 100);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId);
            orderRequest.put("payment_capture", 1); // Auto-capture after payment

            // Add customer details
            JSONObject customerDetails = new JSONObject();
            customerDetails.put("email", customerEmail);
            customerDetails.put("phone", customerPhone);

            // Additional notes
            JSONObject notes = new JSONObject();
            notes.put("bookingId", orderId);
            notes.put("service", "MovieDekho");
            orderRequest.put("notes", notes);

            com.razorpay.Order order = razorpayClient.orders.create(orderRequest);
            JSONObject orderJson = order.toJson();

            RazorpayOrderResponse response = new RazorpayOrderResponse();
            response.setOrderId(orderJson.getString("id"));
            response.setAmount(amount);
            response.setAmountDue(orderJson.getLong("amount_due") / 100.0); // Convert back to rupees
            response.setStatus(orderJson.getString("status"));
            response.setReceiptId(orderJson.getString("receipt"));
            response.setKeyId(razorpayKeyId);
            response.setMessage("Order created successfully");

            log.info("Razorpay order created successfully: {}", response.getOrderId());
            return response;

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: ", e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            log.info("Verifying payment signature for orderId: {}, paymentId: {}", orderId, paymentId);

            String toVerify = orderId + "|" + paymentId;

            String generatedSignature = generateHMACSHA256(toVerify, razorpayKeySecret);

            boolean isValid = generatedSignature.equals(signature);

            if (isValid) {
                log.info("Payment signature verified successfully for orderId: {}", orderId);
            } else {
                log.warn("Payment signature verification failed for orderId: {}", orderId);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying payment signature: ", e);
            return false;
        }
    }

    private String generateHMACSHA256(String toSign, String secret) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(toSign.getBytes());
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public PaymentDetailsResponse getPaymentDetails(String paymentId) {
        try {
            log.info("Fetching payment details for paymentId: {}", paymentId);

            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            JSONObject paymentJson = payment.toJson();

            PaymentDetailsResponse response = new PaymentDetailsResponse();
            response.setPaymentId(paymentId);
            response.setStatus(paymentJson.getString("status"));
            response.setAmount(paymentJson.getLong("amount") / 100.0);
            response.setMethod(paymentJson.getString("method"));
            response.setOrderId(paymentJson.getString("order_id"));
            response.setDescription(paymentJson.optString("description", ""));

            Map<String, Object> details = new HashMap<>();
            details.put("currency", paymentJson.optString("currency", "INR"));
            details.put("fee", paymentJson.optLong("fee", 0) / 100.0);
            details.put("tax", paymentJson.optLong("tax", 0) / 100.0);
            response.setDetails(details);

            log.info("Payment details fetched successfully: {}", paymentId);
            return response;

        } catch (RazorpayException e) {
            log.error("Error fetching payment details: ", e);
            throw new RuntimeException("Failed to fetch payment details: " + e.getMessage(), e);
        }
    }

    public RefundResponse refundPayment(String paymentId, double refundAmount) {
        try {
            log.info("Processing refund for paymentId: {}, amount: {}", paymentId, refundAmount);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (long) (refundAmount * 100));

            com.razorpay.Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            JSONObject refundJson = refund.toJson();

            RefundResponse response = new RefundResponse();
            response.setRefundId(refundJson.getString("id"));
            response.setPaymentId(paymentId);
            response.setAmount(refundJson.getLong("amount") / 100.0);
            response.setStatus(refundJson.getString("status"));
            response.setMessage("Refund processed successfully");

            log.info("Refund processed successfully: {}", response.getRefundId());
            return response;

        } catch (RazorpayException e) {
            log.error("Error processing refund: ", e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
        }
    }

    @Data
    public static class RazorpayOrderResponse {
        private String orderId;
        private double amount;
        private double amountDue;
        private String status;
        private String receiptId;
        private String keyId;
        private String message;

    }

    @Data
    public static class PaymentDetailsResponse {
        private String paymentId;
        private String status;
        private double amount;
        private String method;
        private String orderId;
        private String description;
        private Map<String, Object> details;

    }

    @Data
    public static class RefundResponse {
        private String refundId;
        private String paymentId;
        private double amount;
        private String status;
        private String message;

    }
}
