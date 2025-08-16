package com.movieDekho.MovieDekho.dtos.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.movieDekho.MovieDekho.models.Booking;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class BookingResponse {
    
    private Long bookingId;
    private Long slotId;
    private String movieTitle;
    private String theaterName;
    private String screenType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showDateTime;
    
    private List<String> seatNumbers;
    private String userEmail;
    private Double totalAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingTime;
    
    private Booking.BookingStatus status;
    private String paymentId;
    private String paymentStatus;
    
    // Ticket verification fields
    private String qrCode;
    private boolean verified;
    private String verificationTime;
    private String verifiedBy;
}
