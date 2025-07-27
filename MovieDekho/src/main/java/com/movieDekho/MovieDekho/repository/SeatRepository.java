package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    // Find all seats for a specific slot
    List<Seat> findBySlot(MovieSlot slot);
    
    // Find seats by booking status
    List<Seat> findBySlotAndIsBooked(MovieSlot slot, boolean isBooked);
    
    // Find seat by slot and seat number
    Optional<Seat> findBySlotAndSeatNumber(MovieSlot slot, String seatNumber);
    
    // Find seats by slot and seat numbers (for duplicate check)
    List<Seat> findBySlotAndSeatNumberIn(MovieSlot slot, List<String> seatNumbers);
    
    // Find seats by price range
    List<Seat> findBySlotAndPriceBetween(MovieSlot slot, double minPrice, double maxPrice);
    
    // Count available seats for a slot
    long countBySlotAndIsBooked(MovieSlot slot, boolean isBooked);
    
    // Find seats by slot ordered by seat number
    List<Seat> findBySlotOrderBySeatNumber(MovieSlot slot);
}
