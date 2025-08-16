package com.movieDekho.MovieDekho.service.bookingService;

import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SeatSelectionCacheService {

    private final ConcurrentHashMap<String, CachedSeatSelection> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SeatSelectionCacheService() {
        //Setting time for delete entry is 10min
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 10,10, TimeUnit.MINUTES);
    }

    /**
     * Store seat selection for a user
     */
    public void storeSeatSelection(String userEmail, SeatSelectionResponse seatSelection) {
        String key = generateKey(userEmail, seatSelection.getSlotId());
        CachedSeatSelection cached = new CachedSeatSelection(seatSelection, LocalDateTime.now());
        cache.put(key, cached);
        log.info("Stored seat selection for user: {} - Slot: {}", userEmail, seatSelection.getSlotId());
    }

    /**
     * Retrieve seat selection for a user and slot
     */
    public SeatSelectionResponse getSeatSelection(String userEmail, Long slotId) {
        String key = generateKey(userEmail, slotId);
        CachedSeatSelection cached = cache.get(key);
        
        if (cached == null) {
            log.warn("No seat selection found for user: {} - Slot: {}", userEmail, slotId);
            return null;
        }

        // Check if expired (30 minutes)
        if (cached.getTimestamp().isBefore(LocalDateTime.now().minusMinutes(30))) {
            cache.remove(key);
            log.warn("Expired seat selection removed for user: {} - Slot: {}", userEmail, slotId);
            return null;
        }

        return cached.getSeatSelection();
    }

    /**
     * Remove seat selection after successful booking
     */
    public void removeSeatSelection(String userEmail, Long slotId) {
        String key = generateKey(userEmail, slotId);
        cache.remove(key);
        log.info("Removed seat selection for user: {} - Slot: {}", userEmail, slotId);
    }

    private String generateKey(String userEmail, Long slotId) {
        return userEmail + "_" + slotId;
    }

    private void cleanupExpiredEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        cache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getTimestamp().isBefore(cutoff);
            if (expired) {
                log.debug("Cleaned up expired seat selection: {}", entry.getKey());
            }
            return expired;
        });
    }

    private static class CachedSeatSelection {
        private final SeatSelectionResponse seatSelection;
        private final LocalDateTime timestamp;

        public CachedSeatSelection(SeatSelectionResponse seatSelection, LocalDateTime timestamp) {
            this.seatSelection = seatSelection;
            this.timestamp = timestamp;
        }

        public SeatSelectionResponse getSeatSelection() {
            return seatSelection;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
