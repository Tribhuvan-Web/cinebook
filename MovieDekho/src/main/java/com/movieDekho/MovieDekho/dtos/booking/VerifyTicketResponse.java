package com.movieDekho.MovieDekho.dtos.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyTicketResponse {
    private boolean valid;
    private String message;
    private TicketVerificationDto ticketDetails;
    private String verificationTime;
    
    public VerifyTicketResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}
