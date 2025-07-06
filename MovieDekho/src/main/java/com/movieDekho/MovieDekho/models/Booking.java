package com.movieDekho.MovieDekho.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private MovieSlot slot;

    @ElementCollection
    private List<String> seatNumbers;

    private String userEmail;
    private double totalAmount;
    private LocalDateTime bookingTime;
    private BookingStatus status;

    public enum BookingStatus {
        CONFIRMED, CANCELLED
    }
}