package com.movieDekho.MovieDekho.config.emailconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.email.queue")
@Data
public class EmailQueueConfig {
    
    /**
     * Number of emails to process in each batch
     */
    private int batchSize = 10;
    
    /**
     * Processing interval in milliseconds
     */
    private long processingInterval = 30000; // 30 seconds
    
    /**
     * Maximum retry attempts for failed emails
     */
    private int maxRetries = 3;
    
    /**
     * Base delay between retries in minutes
     */
    private int baseRetryDelayMinutes = 60; // 1 hour
    
    /**
     * Whether to use exponential backoff for retries
     */
    private boolean exponentialBackoff = true;
    
    /**
     * Maximum delay between retries in minutes
     */
    private int maxRetryDelayMinutes = 1440; // 24 hours
    
    /**
     * Number of days to keep sent emails before cleanup
     */
    private int keepSentEmailsDays = 30;
    
    /**
     * Number of days to keep failed emails before cleanup
     */
    private int keepFailedEmailsDays = 7;
    
    /**
     * Whether to enable email queue processing
     */
    private boolean enabled = true;
    
    /**
     * Maximum number of emails to send per day (0 = no limit)
     */
    private int dailyEmailLimit = 300;
    
    /**
     * High priority for welcome emails
     */
    private int welcomeEmailPriority = 5;
    
    /**
     * High priority for OTP emails
     */
    private int otpEmailPriority = 10;
    
    /**
     * Medium priority for password reset emails
     */
    private int passwordResetEmailPriority = 8;
    
    /**
     * Medium priority for admin notifications
     */
    private int adminNotificationPriority = 6;
    
    /**
     * Low priority for general emails
     */
    private int generalEmailPriority = 1;
    
    /**
     * Processing timeout in minutes (for stuck email detection)
     */
    private int processingTimeoutMinutes = 10;
    
    /**
     * Enable detailed logging
     */
    private boolean enableDetailedLogging = true;
    
    /**
     * Pause processing when daily limit is reached
     */
    private boolean pauseOnDailyLimit = true;
}