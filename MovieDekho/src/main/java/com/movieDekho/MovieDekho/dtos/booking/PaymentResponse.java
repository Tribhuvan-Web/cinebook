package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentResponse {
    
    private String paymentId;
    private String status;
    private String approvalUrl;
    private String cancelUrl;
    private String returnUrl;
    private Double amount;
    private String currency;
}
