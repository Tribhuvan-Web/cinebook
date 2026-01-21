package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // ============ BASIC CRUD OPERATIONS ============

    // Find all seats for a specific slot
    List<Seat> findBySlot(MovieSlot slot);

    // Find seats by booking status with ordering
    List<Seat> findBySlotAndIsBookedOrderBySeatNumber(MovieSlot slot, boolean isBooked);
    
    // Legacy method for backward compatibility
    List<Seat> findBySlotAndIsBooked(MovieSlot slot, boolean isBooked);

    // Find seat by slot and seat number
    Optional<Seat> findBySlotAndSeatNumber(MovieSlot slot, String seatNumber);

    // Find seats by slot ordered by seat number
    List<Seat> findBySlotOrderBySeatNumber(MovieSlot slot);

    // ============ OPTIMIZED BATCH OPERATIONS ============

    /**
     * OPTIMIZED: Find existing seat numbers for duplicate checking
     * Returns only seat numbers as Set for O(1) lookup
     * Time Complexity: O(1) database query + O(n) result processing
     */
    @Query("SELECT s.seatNumber FROM Seat s WHERE s.slot.slotId = :slotId AND s.seatNumber IN :seatNumbers")
    Set<String> findExistingSeatNumbers(@Param("slotId") Long slotId, @Param("seatNumbers") Set<String> seatNumbers);

    /**
     * Legacy method - delegates to optimized version
     * @deprecated Use findExistingSeatNumbers for better performance
     */
    List<Seat> findBySlotAndSeatNumberIn(MovieSlot slot, List<String> seatNumbers);

    /**
     * OPTIMIZED: Batch insert with proper batch configuration
     * Uses JPA batch processing for optimal performance
     */
    default List<Seat> saveAllInBatch(List<Seat> seats) {
        return saveAll(seats);
    }

    // ============ OPTIMIZED QUERY OPERATIONS ============

    /**
     * OPTIMIZED: Find seats by price range with ordering
     */
    List<Seat> findBySlotAndPriceBetweenOrderByPrice(MovieSlot slot, double minPrice, double maxPrice);
    
    // Legacy method for backward compatibility
    List<Seat> findBySlotAndPriceBetween(MovieSlot slot, double minPrice, double maxPrice);

    /**
     * OPTIMIZED: Count operations for performance
     */
    long countBySlot(MovieSlot slot);
    long countBySlotAndIsBooked(MovieSlot slot, boolean isBooked);

    /**
     * OPTIMIZED: Pagination support for large datasets
     */
    @Query("SELECT s FROM Seat s WHERE s.slot = :slot ORDER BY s.seatNumber LIMIT :limit OFFSET :offset")
    List<Seat> findBySlotWithPagination(@Param("slot") MovieSlot slot, 
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);

    // ============ RACE CONDITION HANDLING ============

    // Pessimistic locking methods for race condition handling
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds")
    List<Seat> findByIdsWithLock(@Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.slot = :slot AND s.seatNumber IN :seatNumbers")
    List<Seat> findBySlotAndSeatNumbersWithLock(@Param("slot") MovieSlot slot,
            @Param("seatNumbers") List<String> seatNumbers);

    // ============ BULK OPERATIONS ============

    /**
     * OPTIMIZED: Bulk update booking status
     */
    @Modifying
    @Query("UPDATE Seat s SET s.isBooked = :isBooked WHERE s.seatId IN :seatIds")
    int bulkUpdateBookingStatus(@Param("seatIds") List<Long> seatIds, @Param("isBooked") boolean isBooked);

    /**
     * OPTIMIZED: Bulk delete by IDs
     */
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.seatId IN :seatIds")
    int bulkDeleteByIds(@Param("seatIds") List<Long> seatIds);
}
