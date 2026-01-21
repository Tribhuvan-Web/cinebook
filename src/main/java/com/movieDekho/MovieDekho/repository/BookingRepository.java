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

    List<Booking> findByUser(User user);

    List<Booking> findByUserEmail(String userEmail);

    List<Booking> findBySlot(MovieSlot slot);

    List<Booking> findBySlotAndStatus(MovieSlot slot, Booking.BookingStatus status);

    List<Booking> findByBookingTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.bookingTime DESC")
    List<Booking> findRecentBookingsByUser(@Param("user") User user);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.slot = :slot AND b.status = com.movieDekho.MovieDekho.models.Booking$BookingStatus.CONFIRMED")
    List<Booking> findConfirmedBookingsBySlot(@Param("slot") MovieSlot slot);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.slot = :slot AND :seatNumber MEMBER OF b.seatNumbers AND b.status = com.movieDekho.MovieDekho.models.Booking$BookingStatus.CONFIRMED")
    boolean isSeatBookedForSlot(@Param("slot") MovieSlot slot, @Param("seatNumber") String seatNumber);

    @Query("SELECT seatNumber FROM Booking b JOIN b.seatNumbers seatNumber WHERE b.slot = :slot AND b.status = com.movieDekho.MovieDekho.models.Booking$BookingStatus.CONFIRMED")
    List<String> findBookedSeatNumbersBySlot(@Param("slot") MovieSlot slot);

    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie ORDER BY b.bookingTime DESC")
    List<Booking> findAllWithDetails();

    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie WHERE b.userEmail = :userEmail ORDER BY b.bookingTime DESC")
    List<Booking> findByUserEmailWithDetails(@Param("userEmail") String userEmail);

    @Query("SELECT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH s.movie WHERE b.bookingId = :bookingId")
    Optional<Booking> findByIdWithDetails(@Param("bookingId") Long bookingId);

    Optional<Booking> findByQrCode(String qrCode);

    @Query("SELECT b FROM Booking b WHERE b.isVerified = :isVerified")
    List<Booking> findByIsVerified(Boolean isVerified);

    Optional<Booking> findByVerificationToken(String verificationToken);
}
