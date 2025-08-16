package com.movieDekho.MovieDekho.service.temporarySeatLockService;

import com.movieDekho.MovieDekho.models.Seat;
import com.movieDekho.MovieDekho.models.TemporarySeatLock;
import com.movieDekho.MovieDekho.repository.TemporarySeatLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemporarySeatLockService {

    private final TemporarySeatLockRepository temporaryLockRepository;
    private static final int LOCK_DURATION_MINUTES = 3;

    @Transactional
    public List<String> lockSeats(List<Seat> seats, String sessionId) {
        cleanupExpiredLocks();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(LOCK_DURATION_MINUTES);

        // First, release any existing locks for this session
        releaseLocksBySession(sessionId);

        // Check which seats are already locked by other sessions
        List<String> unavailableSeats = seats.stream()
                .filter(seat -> isSeatLocked(seat, sessionId))
                .map(Seat::getSeatNumber)
                .collect(Collectors.toList());

        if (!unavailableSeats.isEmpty()) {
            log.info("Seat selection conflict - seats already locked by other sessions: {}", unavailableSeats);
            return unavailableSeats;
        }

        // Lock all seats for this session
        for (Seat seat : seats) {
            TemporarySeatLock lock = new TemporarySeatLock();
            lock.setSeat(seat);
            lock.setSlot(seat.getSlot());
            lock.setSessionId(sessionId);
            lock.setLockedAt(now);
            lock.setExpiresAt(expiresAt);
            lock.setActive(true);

            temporaryLockRepository.save(lock);
        }

        log.info("Seats locked successfully for session {}: {}", sessionId,
                seats.stream().map(Seat::getSeatNumber).collect(Collectors.toList()));

        return List.of(); // Empty list means all seats were locked successfully
    }

    /**
     * Check if a seat is locked by another session
     */
    public boolean isSeatLocked(Seat seat, String sessionId) {
        return temporaryLockRepository.isSeatLockedByOtherSession(seat, sessionId, LocalDateTime.now());
    }

    /**
     * Release all locks for a session
     */
    @Transactional
    public void releaseLocksBySession(String sessionId) {
        int releasedCount = temporaryLockRepository.releaseLocksBySessionId(sessionId);
        if (releasedCount > 0) {
            log.info("Released {} locks for session: {}", releasedCount, sessionId);
        }
    }

    /**
     * Get a session ID for user (can be based on user email or generate random)
     */
    public String generateSessionId(String userIdentifier) {
        if (userIdentifier != null && !userIdentifier.isEmpty()) {
            return "user_" + userIdentifier.hashCode();
        }
        return "session_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Clean up expired locks automatically
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void cleanupExpiredLocks() {
        int cleanedCount = temporaryLockRepository.deactivateExpiredLocks(LocalDateTime.now());
        if (cleanedCount > 0) {
            log.info("Cleaned up {} expired seat locks", cleanedCount);
        }
    }

    /**
     * Get locks for a session (for debugging/monitoring)
     */
    public List<TemporarySeatLock> getLocksForSession(String sessionId) {
        return temporaryLockRepository.findBySessionIdAndIsActiveTrue(sessionId);
    }
}
