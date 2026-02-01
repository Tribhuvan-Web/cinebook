package com.movieDekho.MovieDekho.service.emailService;

import com.movieDekho.MovieDekho.config.emailconfig.EmailQueueConfig;
import com.movieDekho.MovieDekho.models.EmailQueue;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessorService {

    private final EmailQueueService emailQueueService;
    private final BrevoEmailService brevoEmailService;
    private final EmailQueueConfig emailQueueConfig;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public void processEmailQueue() {
        if (!emailQueueConfig.isEnabled()) {
            return;
        }

        try {
            emailQueueService.resetStuckEmails();
            
            List<EmailQueue> emailsToSend = emailQueueService.getEmailsReadyToSend();
            
            if (emailsToSend.isEmpty()) {
                return;
            }

            if (emailQueueConfig.isEnableDetailedLogging()) {
                log.info("Processing {} emails from queue", emailsToSend.size());
            }

            // Process emails asynchronously
            List<CompletableFuture<Void>> futures = emailsToSend.stream()
                    .map(this::processEmailAsync)
                    .toList();

            // Wait for all emails to be processed (with timeout)
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .join();

        } catch (Exception e) {
            log.error("Error processing email queue", e);
        }
    }

    /**
     * Process a single email asynchronously
     */
    private CompletableFuture<Void> processEmailAsync(EmailQueue email) {
        return CompletableFuture.runAsync(() -> processSingleEmail(email), executorService);
    }

    /**
     * Process a single email
     */
    private void processSingleEmail(EmailQueue email) {
        try {
            // Mark as processing
            emailQueueService.markAsProcessing(email.getId());

            if (emailQueueConfig.isEnableDetailedLogging()) {
                log.info("Processing email - ID: {}, Type: {}, Recipient: {}, Attempt: {}/{}", 
                         email.getId(), email.getEmailType(), email.getRecipientEmail(), 
                         email.getRetryCount() + 1, email.getMaxRetries());
            }

            // Send email through Brevo service
            brevoEmailService.sendEmail(
                    email.getRecipientEmail(), 
                    email.getSubject(), 
                    email.getHtmlContent()
            );

            // Mark as sent
            emailQueueService.markAsSent(email.getId());

            if (emailQueueConfig.isEnableDetailedLogging()) {
                log.info("Email sent successfully - ID: {}, Type: {}, Recipient: {}", 
                         email.getId(), email.getEmailType(), email.getRecipientEmail());
            }

        } catch (Exception e) {
            // Handle different types of errors
            String errorMessage = e.getMessage();
            boolean shouldRetry = shouldRetryBasedOnError(e);

            if (shouldRetry) {
                log.warn("Email sending failed (will retry) - ID: {}, Error: {}", email.getId(), errorMessage);
                emailQueueService.markAsFailed(email.getId(), errorMessage);
            } else {
                log.error("Email sending failed permanently - ID: {}, Error: {}", email.getId(), errorMessage);
                // Mark as failed without retry
                emailQueueService.markAsFailed(email.getId(), "Non-retryable error: " + errorMessage);
            }
        }
    }

    /**
     * Determine if we should retry based on the error type
     */
    private boolean shouldRetryBasedOnError(Exception e) {
        String errorMessage = e.getMessage();
        
        if (errorMessage == null) {
            return true; // Retry for unknown errors
        }

        errorMessage = errorMessage.toLowerCase();

        // Don't retry for these errors
        if (errorMessage.contains("invalid email") ||
            errorMessage.contains("blacklisted") ||
            errorMessage.contains("unsubscribed") ||
            errorMessage.contains("401") ||  // Authentication error
            errorMessage.contains("403")) {  // Forbidden
            return false;
        }

        // Retry for these errors
        if (errorMessage.contains("402") ||   // Payment/limit issues
            errorMessage.contains("429") ||   // Rate limiting
            errorMessage.contains("500") ||   // Server errors
            errorMessage.contains("502") ||   // Bad gateway
            errorMessage.contains("503") ||   // Service unavailable
            errorMessage.contains("504") ||   // Gateway timeout
            errorMessage.contains("timeout") ||
            errorMessage.contains("connection")) {
            return true;
        }

        // Default to retry for unknown errors
        return true;
    }

    /**
     * Send high priority email immediately (bypass queue for critical emails)
     */
    public boolean sendImmediateEmail(String recipientEmail, String subject, String htmlContent, 
                                     EmailQueue.EmailType emailType) {
        try {
            log.info("Sending immediate email - Type: {}, Recipient: {}", emailType, recipientEmail);
            
            brevoEmailService.sendEmail(recipientEmail, subject, htmlContent);
            
            log.info("Immediate email sent successfully - Type: {}, Recipient: {}", emailType, recipientEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send immediate email - Type: {}, Recipient: {}, Error: {}", 
                     emailType, recipientEmail, e.getMessage());
            
            // Fallback to queue
            emailQueueService.queueEmail(recipientEmail, subject, htmlContent, emailType, null);
            log.info("Email added to queue as fallback - Type: {}, Recipient: {}", emailType, recipientEmail);
            
            return false;
        }
    }

    /**
     * Cleanup method to be called on application shutdown
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}