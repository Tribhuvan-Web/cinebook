package com.movieDekho.MovieDekho.config.reqconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for temporary seat lock cleanup
 */
@Component
@ConfigurationProperties(prefix = "movie-dekho.cleanup")
@Data
public class CleanupConfig {
    
    /**
     * How often to run cleanup (in milliseconds)
     * Default: 3600000 (1 hour)
     */
    private long cleanupInterval = 3600000; // 1 hour
    
    /**
     * How old locks should be before deep cleanup (in hours)
     * Default: 1 hour
     */
    private int deepCleanupHours = 1;
    
    /**
     * Whether to enable automatic cleanup
     * Default: true
     */
    private boolean enableAutoCleanup = true;
    
    /**
     * Whether to log successful cleanup operations
     * Default: true
     */
    private boolean logCleanupOperations = true;
}
