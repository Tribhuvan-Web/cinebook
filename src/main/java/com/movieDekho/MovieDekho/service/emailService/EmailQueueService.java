package com.movieDekho.MovieDekho.service.emailService;

import com.movieDekho.MovieDekho.config.emailconfig.EmailQueueConfig;
import com.movieDekho.MovieDekho.models.EmailQueue;
import com.movieDekho.MovieDekho.repository.EmailQueueRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueConfig emailQueueConfig;

    @Transactional
    public void queueEmail(String recipientEmail, String subject, String htmlContent,
            EmailQueue.EmailType emailType, Long relatedEntityId) {

        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setRecipientEmail(recipientEmail);
        emailQueue.setSubject(subject);
        emailQueue.setHtmlContent(htmlContent);
        emailQueue.setEmailType(emailType);
        emailQueue.setRelatedEntityId(relatedEntityId);
        emailQueue.setPriority(getEmailPriority(emailType));
        emailQueue.setMaxRetries(emailQueueConfig.getMaxRetries());
        emailQueue.setStatus(EmailQueue.EmailStatus.PENDING);
        emailQueue.setScheduledAt(LocalDateTime.now());

        EmailQueue savedEmail = emailQueueRepository.save(emailQueue);

        if (emailQueueConfig.isEnableDetailedLogging()) {
            log.info("Email queued successfully - ID: {}, Type: {}, Recipient: {}, Priority: {}",
                    savedEmail.getId(), emailType, recipientEmail, savedEmail.getPriority());
        }

    }

    /**
     * Queue an email with scheduled time
     */
    @Transactional
    public EmailQueue queueScheduledEmail(String recipientEmail, String subject, String htmlContent,
            EmailQueue.EmailType emailType, Long relatedEntityId,
            LocalDateTime scheduledAt) {

        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setRecipientEmail(recipientEmail);
        emailQueue.setSubject(subject);
        emailQueue.setHtmlContent(htmlContent);
        emailQueue.setEmailType(emailType);
        emailQueue.setRelatedEntityId(relatedEntityId);
        emailQueue.setPriority(getEmailPriority(emailType));
        emailQueue.setMaxRetries(emailQueueConfig.getMaxRetries());
        emailQueue.setStatus(EmailQueue.EmailStatus.SCHEDULED);
        emailQueue.setScheduledAt(scheduledAt);

        EmailQueue savedEmail = emailQueueRepository.save(emailQueue);

        log.info("Email scheduled successfully - ID: {}, Type: {}, Recipient: {}, Scheduled: {}",
                savedEmail.getId(), emailType, recipientEmail, scheduledAt);

        return savedEmail;
    }

    /**
     * Get emails ready to be processed
     */
    @Transactional(readOnly = true)
    public List<EmailQueue> getEmailsReadyToSend() {
        if (!emailQueueConfig.isEnabled()) {
            return List.of();
        }

        // Check daily limit if enabled
        if (emailQueueConfig.getDailyEmailLimit() > 0 && emailQueueConfig.isPauseOnDailyLimit()) {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            long sentToday = emailQueueRepository.countSentEmailsInPeriod(startOfDay, endOfDay);

            if (sentToday >= emailQueueConfig.getDailyEmailLimit()) {
                log.warn("Daily email limit reached ({}/{}). Pausing email processing.",
                        sentToday, emailQueueConfig.getDailyEmailLimit());
                return List.of();
            }
        }

        PageRequest pageRequest = PageRequest.of(0, emailQueueConfig.getBatchSize());
        return emailQueueRepository.findEmailsReadyToSend(LocalDateTime.now(), pageRequest);
    }

    /**
     * Mark email as processing
     */
    @Transactional
    public void markAsProcessing(Long emailId) {
        emailQueueRepository.findById(emailId).ifPresent(email -> {
            email.setStatus(EmailQueue.EmailStatus.PROCESSING);
            email.setLastAttemptAt(LocalDateTime.now());
            emailQueueRepository.save(email);
        });
    }

    /**
     * Mark email as sent successfully
     */
    @Transactional
    public void markAsSent(Long emailId) {
        emailQueueRepository.findById(emailId).ifPresent(email -> {
            email.setStatus(EmailQueue.EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            emailQueueRepository.save(email);

            if (emailQueueConfig.isEnableDetailedLogging()) {
                log.info("Email marked as sent - ID: {}, Type: {}, Recipient: {}",
                        emailId, email.getEmailType(), email.getRecipientEmail());
            }
        });
    }

    /**
     * Mark email as failed and schedule retry if possible
     */
    @Transactional
    public void markAsFailed(Long emailId, String errorMessage) {
        emailQueueRepository.findById(emailId).ifPresent(email -> {
            email.setRetryCount(email.getRetryCount() + 1);
            email.setErrorMessage(errorMessage);
            email.setLastAttemptAt(LocalDateTime.now());

            if (email.getRetryCount() < email.getMaxRetries()) {
                // Schedule for retry
                LocalDateTime nextRetry = calculateNextRetryTime(email.getRetryCount());
                email.setScheduledAt(nextRetry);
                email.setStatus(EmailQueue.EmailStatus.PENDING);

                log.warn("Email failed but will retry - ID: {}, Attempt: {}/{}, Next retry: {}, Error: {}",
                        emailId, email.getRetryCount(), email.getMaxRetries(), nextRetry, errorMessage);
            } else {
                // Permanently failed
                email.setStatus(EmailQueue.EmailStatus.FAILED);

                log.error("Email permanently failed - ID: {}, Type: {}, Recipient: {}, Error: {}",
                        emailId, email.getEmailType(), email.getRecipientEmail(), errorMessage);
            }

            emailQueueRepository.save(email);
        });
    }

    /**
     * Reset stuck emails back to pending
     */
    @Transactional
    public int resetStuckEmails() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(emailQueueConfig.getProcessingTimeoutMinutes());
        int resetCount = emailQueueRepository.resetStuckEmails(cutoffTime);

        if (resetCount > 0) {
            log.warn("Reset {} stuck emails back to pending status", resetCount);
        }

        return resetCount;
    }

    /**
     * Cleanup old emails
     */
    @Transactional
    public void cleanupOldEmails() {
        LocalDateTime sentEmailsCutoff = LocalDateTime.now().minusDays(emailQueueConfig.getKeepSentEmailsDays());
        LocalDateTime failedEmailsCutoff = LocalDateTime.now().minusDays(emailQueueConfig.getKeepFailedEmailsDays());

        // Cancel old pending/failed emails
        int cancelledCount = emailQueueRepository.cancelOldEmails(failedEmailsCutoff);

        // Delete old sent emails
        List<EmailQueue> oldSentEmails = emailQueueRepository.findAll().stream()
                .filter(email -> email.getStatus() == EmailQueue.EmailStatus.SENT)
                .filter(email -> email.getSentAt() != null && email.getSentAt().isBefore(sentEmailsCutoff))
                .toList();

        if (!oldSentEmails.isEmpty()) {
            emailQueueRepository.deleteAll(oldSentEmails);
            log.info("Deleted {} old sent emails", oldSentEmails.size());
        }

        if (cancelledCount > 0) {
            log.info("Cancelled {} old pending/failed emails", cancelledCount);
        }
    }

    /**
     * Get queue statistics
     */
    @Transactional(readOnly = true)
    public EmailQueueStats getQueueStats() {
        EmailQueueStats stats = new EmailQueueStats();

        stats.setPendingCount(emailQueueRepository.countByStatus(EmailQueue.EmailStatus.PENDING));
        stats.setProcessingCount(emailQueueRepository.countByStatus(EmailQueue.EmailStatus.PROCESSING));
        stats.setSentCount(emailQueueRepository.countByStatus(EmailQueue.EmailStatus.SENT));
        stats.setFailedCount(emailQueueRepository.countByStatus(EmailQueue.EmailStatus.FAILED));
        stats.setScheduledCount(emailQueueRepository.countByStatus(EmailQueue.EmailStatus.SCHEDULED));

        // Today's sent count
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        stats.setSentTodayCount(emailQueueRepository.countSentEmailsInPeriod(startOfDay, endOfDay));

        return stats;
    }

    /**
     * Get email priority based on type
     */
    private int getEmailPriority(EmailQueue.EmailType emailType) {
        return switch (emailType) {
            case OTP -> emailQueueConfig.getOtpEmailPriority();
            case PASSWORD_RESET -> emailQueueConfig.getPasswordResetEmailPriority();
            case WELCOME -> emailQueueConfig.getWelcomeEmailPriority();
            case ADMIN_REGISTRATION_NOTIFICATION, ADMIN_APPROVAL_NOTIFICATION ->
                emailQueueConfig.getAdminNotificationPriority();
            default -> emailQueueConfig.getGeneralEmailPriority();
        };
    }

    /**
     * Calculate next retry time with exponential backoff
     */
    private LocalDateTime calculateNextRetryTime(int retryCount) {
        int delayMinutes = emailQueueConfig.getBaseRetryDelayMinutes();

        if (emailQueueConfig.isExponentialBackoff()) {
            // Exponential backoff: base * 2^(retryCount-1)
            delayMinutes = emailQueueConfig.getBaseRetryDelayMinutes() * (int) Math.pow(2, retryCount - 1);
            delayMinutes = Math.min(delayMinutes, emailQueueConfig.getMaxRetryDelayMinutes());
        }

        return LocalDateTime.now().plusMinutes(delayMinutes);
    }

    @Data
    public static class EmailQueueStats {
        private long pendingCount;
        private long processingCount;
        private long sentCount;
        private long failedCount;
        private long scheduledCount;
        private long sentTodayCount;
    }
}