package com.movieDekho.MovieDekho.config.emailconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.email.queue")
@Data
public class EmailQueueConfig {
    
    private int batchSize = 10;
    private long processingInterval = 30000; 
    private int maxRetries = 3;
    private int baseRetryDelayMinutes = 60; 
    private boolean exponentialBackoff = true;
    private int maxRetryDelayMinutes = 1440;
    private int keepSentEmailsDays = 30;
    private int keepFailedEmailsDays = 7;
    private boolean enabled = true;
    private int dailyEmailLimit = 300;
    private int welcomeEmailPriority = 5;
    private int otpEmailPriority = 10;
    private int passwordResetEmailPriority = 8;
    private int adminNotificationPriority = 6;
    private int generalEmailPriority = 1;
    private int processingTimeoutMinutes = 10;
    private boolean enableDetailedLogging = true;
    private boolean pauseOnDailyLimit = true;
}