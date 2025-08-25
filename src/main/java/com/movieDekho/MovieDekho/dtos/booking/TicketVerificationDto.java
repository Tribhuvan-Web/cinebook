package com.movieDekho.MovieDekho.dtos.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketVerificationDto {
    private String verificationToken;
    private String qrCode; // This will be token + random string
    private Long bookingId;
    private String movieTitle;
    private String cinemaName;
    private String showDate;
    private String showTime;
    private String seatNumbers;
    private String userEmail;
    private boolean verified;
    private String verificationTime;
    private String verifiedBy;
}
