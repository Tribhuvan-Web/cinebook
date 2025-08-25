package com.movieDekho.MovieDekho.service.ticketVerificationService;

import com.movieDekho.MovieDekho.dtos.booking.TicketVerificationDto;
import com.movieDekho.MovieDekho.dtos.booking.VerifyTicketResponse;

public interface TicketVerificationService {
    String generateVerificationToken();
    String generateRandomString();
    String generateQRCode(String verificationToken, String randomString);
    VerifyTicketResponse verifyTicket(String qrCode, String adminEmail);
    TicketVerificationDto getTicketVerificationDetails(Long bookingId);
    void generateVerificationDataForBooking(Long bookingId);
}
