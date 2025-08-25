package com.movieDekho.MovieDekho.service.ticketVerificationService;

import com.movieDekho.MovieDekho.dtos.booking.TicketVerificationDto;
import com.movieDekho.MovieDekho.dtos.booking.VerifyTicketResponse;
import com.movieDekho.MovieDekho.models.Booking;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketVerificationServiceImpl implements TicketVerificationService {

    private final BookingRepository bookingRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_STRING_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateVerificationToken() {
        // Generate a UUID-based token and encode it
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes());
    }

    @Override
    public String generateRandomString() {
        StringBuilder sb = new StringBuilder(RANDOM_STRING_LENGTH);
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Override
    public String generateQRCode(String verificationToken, String randomString) {
        // Combine token and random string with a separator
        return verificationToken + ":" + randomString;
    }

    @Override
    @Transactional
    public void generateVerificationDataForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        if (booking.getVerificationToken() == null || booking.getRandomString() == null) {
            String verificationToken = generateVerificationToken();
            String randomString = generateRandomString();
            String qrCode = generateQRCode(verificationToken, randomString);
            
            booking.setVerificationToken(verificationToken);
            booking.setRandomString(randomString);
            booking.setQrCode(qrCode);
            
            bookingRepository.save(booking);
            log.info("Generated verification data for booking ID: {}", bookingId);
        }
    }

    @Override
    @Transactional
    public VerifyTicketResponse verifyTicket(String qrCode, String adminEmail) {
        try {
            log.info("Verifying ticket with QR code for admin: {}", adminEmail);
            
            // Find booking by QR code
            Booking booking = bookingRepository.findByQrCode(qrCode)
                    .orElse(null);
            
            if (booking == null) {
                log.warn("Invalid QR code provided: {}", qrCode.substring(0, Math.min(10, qrCode.length())) + "...");
                return new VerifyTicketResponse(false, "Invalid ticket QR code");
            }
            
            // Check if booking is confirmed
            if (!Booking.BookingStatus.CONFIRMED.equals(booking.getStatus())) {
                log.warn("Ticket not confirmed for booking ID: {}", booking.getBookingId());
                return new VerifyTicketResponse(false, "Ticket is not confirmed");
            }
            
            // Check if already verified
            if (Boolean.TRUE.equals(booking.getIsVerified())) {
                log.info("Ticket already verified for booking ID: {} at {}", 
                        booking.getBookingId(), booking.getVerificationTime());
                
                TicketVerificationDto details = createTicketVerificationDto(booking);
                return new VerifyTicketResponse(true, "Ticket already verified", details, 
                        booking.getVerificationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // Verify the ticket
            booking.setIsVerified(true);
            booking.setVerificationTime(LocalDateTime.now());
            booking.setVerifiedBy(adminEmail);
            
            bookingRepository.save(booking);
            
            TicketVerificationDto details = createTicketVerificationDto(booking);
            
            log.info("Ticket verified successfully for booking ID: {} by admin: {}", 
                    booking.getBookingId(), adminEmail);
            
            return new VerifyTicketResponse(true, "Ticket verified successfully", details, 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
        } catch (Exception e) {
            log.error("Error verifying ticket: ", e);
            return new VerifyTicketResponse(false, "Error verifying ticket: " + e.getMessage());
        }
    }

    @Override
    public TicketVerificationDto getTicketVerificationDetails(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        return createTicketVerificationDto(booking);
    }

    private TicketVerificationDto createTicketVerificationDto(Booking booking) {
        TicketVerificationDto dto = new TicketVerificationDto();
        dto.setVerificationToken(booking.getVerificationToken());
        dto.setQrCode(booking.getQrCode());
        dto.setBookingId(booking.getBookingId());
        dto.setMovieTitle(booking.getSlot().getMovie().getTitle());
        dto.setCinemaName(booking.getSlot().getTheaterName()); // Using theaterName instead of cinema
        dto.setShowDate(booking.getSlot().getShowDate().toString());
        dto.setShowTime(booking.getSlot().getStartTime().toString()); // Using startTime instead of showTime
        dto.setSeatNumbers(String.join(", ", booking.getSeatNumbers()));
        dto.setUserEmail(booking.getUserEmail());
        dto.setVerified(Boolean.TRUE.equals(booking.getIsVerified()));
        
        if (booking.getVerificationTime() != null) {
            dto.setVerificationTime(booking.getVerificationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setVerifiedBy(booking.getVerifiedBy());
        
        return dto;
    }
}
