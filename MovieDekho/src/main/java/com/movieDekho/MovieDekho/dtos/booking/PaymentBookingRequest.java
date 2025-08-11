package com.movieDekho.MovieDekho.dtos.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentBookingRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Payment method is required")
    private String paymentMethod = "MOCK_PAYMENT";

    @NotNull(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
    private String cardNumber;

    @NotNull(message = "Card holder name is required")
    @Size(min = 2, max = 50, message = "Card holder name must be between 2 and 50 characters")
    private String cardHolderName;

    @NotNull(message = "Expiry date is required")
    @Pattern(regexp = "\\d{2}/\\d{2}", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    @NotNull(message = "CVV is required")
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    @Pattern(regexp = "\\d+", message = "CVV must contain only digits")
    private String cvv;
}
