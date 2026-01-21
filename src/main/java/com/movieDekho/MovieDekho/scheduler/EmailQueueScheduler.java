package com.movieDekho.MovieDekho.scheduler;

import com.movieDekho.MovieDekho.config.emailconfig.EmailQueueConfig;
import com.movieDekho.MovieDekho.service.emailService.EmailProcessorService;
import com.movieDekho.MovieDekho.service.emailService.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueScheduler {

    private final EmailProcessorService emailProcessorService;
    private final EmailQueueService emailQueueService;
    private final EmailQueueConfig emailQueueConfig;

    /**
     * Process email queue at configured intervals
     */
    @Scheduled(fixedDelayString = "#{@emailQueueConfig.processingInterval}")
    public void processEmailQueue() {
        if (!emailQueueConfig.isEnabled()) {
            return;
        }

        try {
            if (emailQueueConfig.isEnableDetailedLogging()) {
                var stats = emailQueueService.getQueueStats();
                if (stats.getPendingCount() > 0 || stats.getProcessingCount() > 0) {
                    log.info("Email queue stats: {}", stats);
                }
            }

            emailProcessorService.processEmailQueue();
            
        } catch (Exception e) {
            log.error("Error in email queue processing scheduler", e);
        }
    }

    /**
     * Cleanup old emails daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldEmails() {
        try {
            log.info("Starting email queue cleanup...");
            emailQueueService.cleanupOldEmails();
            log.info("Email queue cleanup completed");
            
        } catch (Exception e) {
            log.error("Error during email queue cleanup", e);
        }
    }

    /**
     * Log queue statistics every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void logQueueStatistics() {
        if (!emailQueueConfig.isEnabled()) {
            return;
        }

        try {
            var stats = emailQueueService.getQueueStats();
            
            // Only log if there's activity
            if (stats.getPendingCount() > 0 || stats.getProcessingCount() > 0 || 
                stats.getFailedCount() > 0 || stats.getSentTodayCount() > 0) {
                
                log.info("Hourly Email Queue Statistics: {}", stats);
                
                // Warn if failed count is high
                if (stats.getFailedCount() > 10) {
                    log.warn("High number of failed emails detected: {}. Please check email service configuration.", 
                             stats.getFailedCount());
                }
                
                // Warn if approaching daily limit
                if (emailQueueConfig.getDailyEmailLimit() > 0) {
                    double usagePercent = (stats.getSentTodayCount() * 100.0) / emailQueueConfig.getDailyEmailLimit();
                    if (usagePercent > 80) {
                        log.warn("Daily email limit usage at {:.1f}% ({}/{})", 
                                usagePercent, stats.getSentTodayCount(), emailQueueConfig.getDailyEmailLimit());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error logging queue statistics", e);
        }
    }

    /**
     * Reset stuck emails every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void resetStuckEmails() {
        if (!emailQueueConfig.isEnabled()) {
            return;
        }

        try {
            int resetCount = emailQueueService.resetStuckEmails();
            if (resetCount > 0) {
                log.info("Reset {} stuck emails", resetCount);
            }
            
        } catch (Exception e) {
            log.error("Error resetting stuck emails", e);
        }
    }
}