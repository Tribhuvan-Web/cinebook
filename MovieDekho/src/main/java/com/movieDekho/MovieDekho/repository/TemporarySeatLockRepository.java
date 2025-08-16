package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.Seat;
import com.movieDekho.MovieDekho.models.TemporarySeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TemporarySeatLockRepository extends JpaRepository<TemporarySeatLock, Long> {
    
    // Find active locks for specific seats
    @Query("SELECT tsl FROM TemporarySeatLock tsl WHERE tsl.seat IN :seats AND tsl.isActive = true AND tsl.expiresAt > :currentTime")
    List<TemporarySeatLock> findActiveLocksBySeats(@Param("seats") List<Seat> seats, @Param("currentTime") LocalDateTime currentTime);
    
    // Find locks by session ID
    List<TemporarySeatLock> findBySessionIdAndIsActiveTrue(String sessionId);
    
    // Find active lock for specific seat
    @Query("SELECT tsl FROM TemporarySeatLock tsl WHERE tsl.seat = :seat AND tsl.isActive = true AND tsl.expiresAt > :currentTime")
    Optional<TemporarySeatLock> findActiveLockBySeat(@Param("seat") Seat seat, @Param("currentTime") LocalDateTime currentTime);
    
    // Clean up expired locks
    @Modifying
    @Query("UPDATE TemporarySeatLock tsl SET tsl.isActive = false WHERE tsl.expiresAt <= :currentTime AND tsl.isActive = true")
    int deactivateExpiredLocks(@Param("currentTime") LocalDateTime currentTime);
    
    // Release locks by session ID
    @Modifying
    @Query("UPDATE TemporarySeatLock tsl SET tsl.isActive = false WHERE tsl.sessionId = :sessionId AND tsl.isActive = true")
    int releaseLocksBySessionId(@Param("sessionId") String sessionId);
    
    // Check if seat is locked by different session
    @Query("SELECT COUNT(tsl) > 0 FROM TemporarySeatLock tsl WHERE tsl.seat = :seat AND tsl.sessionId != :sessionId AND tsl.isActive = true AND tsl.expiresAt > :currentTime")
    boolean isSeatLockedByOtherSession(@Param("seat") Seat seat, @Param("sessionId") String sessionId, @Param("currentTime") LocalDateTime currentTime);
    
    // Cleanup methods for scheduler
    @Modifying
    @Query("DELETE FROM TemporarySeatLock tsl WHERE tsl.expiresAt < :cutoffTime")
    int deleteByExpiresAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Query("DELETE FROM TemporarySeatLock tsl WHERE tsl.lockedAt < :cutoffTime")
    int deleteByLockedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Delete all inactive locks
    @Modifying
    @Query("DELETE FROM TemporarySeatLock tsl WHERE tsl.isActive = false")
    int deleteInactiveLocks();
}
