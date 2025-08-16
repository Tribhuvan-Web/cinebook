package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.Booking;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.models.MovieSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find bookings by user
    List<Booking> findByUser(User user);
    
    // Find bookings by user email
    List<Booking> findByUserEmail(String userEmail);
    
    // Find bookings by user and status
    List<Booking> findByUserAndStatus(User user, Booking.BookingStatus status);
    
    // Find bookings by slot
    List<Booking> findBySlot(MovieSlot slot);
    
    // Find bookings by slot and status
    List<Booking> findBySlotAndStatus(MovieSlot slot, Booking.BookingStatus status);
    
    // Find booking by payment ID
    Optional<Booking> findByPaymentId(String paymentId);
    
    // Find bookings within date range
    List<Booking> findByBookingTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.bookingTime DESC")
    List<Booking> findRecentBookingsByUser(@Param("user") User user);
    
    // Get booking statistics
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") Booking.BookingStatus status);
    
    // Find bookings by movie slot and confirmed status
    @Query("SELECT b FROM Booking b WHERE b.slot = :slot AND b.status = com.movieDekho.MovieDekho.models.Booking$BookingStatus.CONFIRMED")
    List<Booking> findConfirmedBookingsBySlot(@Param("slot") MovieSlot slot);
    
    // Check if seat is already booked for a slot
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.slot = :slot AND :seatNumber MEMBER OF b.seatNumbers AND b.status = com.movieDekho.MovieDekho.models.Booking$BookingStatus.CONFIRMED")
    boolean isSeatBookedForSlot(@Param("slot") MovieSlot slot, @Param("seatNumber") String seatNumber);
    
    // Find all bookings with proper fetch joins to avoid lazy loading issues
    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie ORDER BY b.bookingTime DESC")
    List<Booking> findAllWithDetails();
    
    // Find bookings by user email with fetch joins
    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie WHERE b.userEmail = :userEmail ORDER BY b.bookingTime DESC")
    List<Booking> findByUserEmailWithDetails(@Param("userEmail") String userEmail);
    
    // Find booking by ID with fetch joins
    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie WHERE b.bookingId = :bookingId")
    Optional<Booking> findByIdWithDetails(@Param("bookingId") Long bookingId);
    
    // Ticket verification related methods
    Optional<Booking> findByQrCode(String qrCode);
    
    @Query("SELECT b FROM Booking b WHERE b.isVerified = :isVerified")
    List<Booking> findByIsVerified(Boolean isVerified);
    
    // Find booking by verification token
    Optional<Booking> findByVerificationToken(String verificationToken);
}
