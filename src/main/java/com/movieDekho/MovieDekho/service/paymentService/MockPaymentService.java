package com.movieDekho.MovieDekho.service.paymentService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MockPaymentService {

    /**
     * Process mock payment
     * @param amount Payment amount
     * @param paymentMethod Payment method (MOCK_PAYMENT, CREDIT_CARD, DEBIT_CARD, etc.)
     * @param cardNumber Card number (for validation)
     * @param cardHolderName Card holder name
     * @param expiryDate Card expiry date (MM/YY)
     * @param cvv Card CVV
     * @return Payment response
     */
    public PaymentResponse processPayment(double amount, String paymentMethod, 
                                        String cardNumber, String cardHolderName, 
                                        String expiryDate, String cvv) {
        
        log.info("Processing mock payment for amount: {}", amount);
        
        try {
            // Mock payment processing logic
            PaymentResponse response = new PaymentResponse();
            
            // Generate mock payment ID
            String paymentId = "MOCK_PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            response.setPaymentId(paymentId);
            response.setAmount(amount);
            response.setPaymentMethod(paymentMethod);
            
            // Simulate payment validation
            if (isValidPaymentDetails(cardNumber, cardHolderName, expiryDate, cvv)) {
                // 95% success rate for mock payments
                boolean paymentSuccessful = Math.random() < 0.95;
                
                if (paymentSuccessful) {
                    response.setStatus("SUCCESS");
                    response.setMessage("Payment processed successfully");
                    response.setTransactionId("TXN_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
                    
                    // Add mock payment details
                    Map<String, Object> paymentDetails = new HashMap<>();
                    paymentDetails.put("gateway", "MockPayment Gateway");
                    paymentDetails.put("processingTime", "2.3s");
                    paymentDetails.put("maskedCardNumber", maskCardNumber(cardNumber));
                    paymentDetails.put("cardType", getCardType(cardNumber));
                    response.setPaymentDetails(paymentDetails);
                    
                    log.info("Mock payment successful: {}", paymentId);
                } else {
                    response.setStatus("FAILED");
                    response.setMessage("Payment failed due to insufficient funds or network error");
                    log.warn("Mock payment failed: {}", paymentId);
                }
            } else {
                response.setStatus("FAILED");
                response.setMessage("Invalid payment details provided");
                log.warn("Mock payment failed due to invalid details: {}", paymentId);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing mock payment: ", e);
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentId("ERROR_" + UUID.randomUUID().toString().substring(0, 8));
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Payment processing error: " + e.getMessage());
            errorResponse.setAmount(amount);
            errorResponse.setPaymentMethod(paymentMethod);
            return errorResponse;
        }
    }
    
    /**
     * Refund mock payment
     */
    public PaymentResponse refundPayment(String paymentId, double amount) {
        log.info("Processing mock refund for payment: {} amount: {}", paymentId, amount);
        
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setAmount(amount);
        response.setPaymentMethod("REFUND");
        response.setStatus("SUCCESS");
        response.setMessage("Refund processed successfully");
        response.setTransactionId("REFUND_" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        
        return response;
    }
    
    private boolean isValidPaymentDetails(String cardNumber, String cardHolderName, 
                                        String expiryDate, String cvv) {
        // Basic validation for mock payment
        return cardNumber != null && cardNumber.length() >= 13 && cardNumber.length() <= 19
                && cardHolderName != null && !cardHolderName.trim().isEmpty()
                && expiryDate != null && expiryDate.matches("\\d{2}/\\d{2}")
                && cvv != null && cvv.length() >= 3 && cvv.length() <= 4;
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    private String getCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }
        
        String firstDigit = cardNumber.substring(0, 1);
        switch (firstDigit) {
            case "4": return "Visa";
            case "5": return "Mastercard";
            case "3": return "American Express";
            case "6": return "Discover";
            default: return "Generic";
        }
    }
    
    /**
     * Payment Response DTO
     */
    public static class PaymentResponse {
        private String paymentId;
        private String transactionId;
        private double amount;
        private String paymentMethod;
        private String status; // SUCCESS, FAILED, PENDING
        private String message;
        private Map<String, Object> paymentDetails;
        
        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Map<String, Object> getPaymentDetails() { return paymentDetails; }
        public void setPaymentDetails(Map<String, Object> paymentDetails) { this.paymentDetails = paymentDetails; }
    }
}
