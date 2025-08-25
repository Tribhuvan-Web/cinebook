package com.movieDekho.MovieDekho.scheduler;

import com.movieDekho.MovieDekho.config.reqconfig.CleanupConfig;
import com.movieDekho.MovieDekho.repository.TemporarySeatLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduler to automatically clean up expired temporary seat locks from the database
 * Runs every hour to keep the database clean and cost-effective
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "movie-dekho.cleanup.enable-auto-cleanup", havingValue = "true", matchIfMissing = true)
public class TemporarySeatLockCleanupScheduler {

    private final TemporarySeatLockRepository temporarySeatLockRepository;
    private final CleanupConfig cleanupConfig;

    /**
     * Cleanup expired temporary seat locks every hour
     * This keeps the database clean and prevents unnecessary storage costs
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    @Transactional
    public void cleanupExpiredTemporarySeatLocks() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now();
            
            // Delete all locks that have expired (expiresAt < now)
            int deletedCount = temporarySeatLockRepository.deleteByExpiresAtBefore(cutoffTime);
            
            if (deletedCount > 0 && cleanupConfig.isLogCleanupOperations()) {
                log.info("Hourly cleanup: Deleted {} expired temporary seat locks", deletedCount);
            } else if (cleanupConfig.isLogCleanupOperations()) {
                log.debug("Hourly cleanup: No expired temporary seat locks found");
            }
            
        } catch (Exception e) {
            log.error("Error during temporary seat lock cleanup: ", e);
        }
    }

    /**
     * Additional cleanup for locks older than 1 hour (safety net)
     * This ensures even if locks somehow don't get cleaned up, they get removed
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 1800000) // Run every hour, start after 30 minutes
    @Transactional
    public void deepCleanupOldLocks() {
        try {
            LocalDateTime oldCutoffTime = LocalDateTime.now().minusHours(cleanupConfig.getDeepCleanupHours());
            
            // Delete any locks older than configured hours (safety cleanup)
            int deletedCount = temporarySeatLockRepository.deleteByLockedAtBefore(oldCutoffTime);
            
            if (deletedCount > 0) {
                log.warn("Deep cleanup: Removed {} old temporary seat locks (older than {} hour(s))", 
                        deletedCount, cleanupConfig.getDeepCleanupHours());
            }
            
        } catch (Exception e) {
            log.error("Error during deep cleanup of temporary seat locks: ", e);
        }
    }

    /**
     * Manual cleanup method that can be called if needed
     * Useful for testing or manual intervention
     */
    public int manualCleanup() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = temporarySeatLockRepository.deleteByExpiresAtBefore(now);
            log.info("Manual cleanup: Deleted {} expired temporary seat locks", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error during manual cleanup: ", e);
            return 0;
        }
    }
}
