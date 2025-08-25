package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BookingRequest {

    private Long slotId;
    private List<String> seatNumbers;
    private Double ticketFee;
    private Double convenienceFee;
    private Double totalAmount;
    private String paymentMethod = "MOCK_PAYMENT";
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
}
