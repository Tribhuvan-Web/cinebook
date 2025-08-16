package com.movieDekho.MovieDekho.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private MovieSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "booking_seats", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "seat_number")
    private List<String> seatNumbers;

    private String userEmail;
    private double totalAmount;
    private LocalDateTime bookingTime;
    private BookingStatus status;

    // Payment related fields
    private String paymentMethod;
    private String paymentId;
    private String paymentStatus;

    @Column(columnDefinition = "TEXT")
    private String paymentDetails; // Store additional payment info as JSON

    // Ticket verification fields
    @Column(name = "verification_token", length = 255)
    private String verificationToken;
    
    @Column(name = "random_string", length = 100)
    private String randomString;
    
    @Column(name = "qr_code", length = 355)
    private String qrCode; // verification_token + random_string
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "verification_time")
    private LocalDateTime verificationTime;
    
    @Column(name = "verified_by")
    private String verifiedBy; // Admin email who verified

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, PAYMENT_FAILED
    }
}