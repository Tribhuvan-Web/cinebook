package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.service.emailService.EmailQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/email-queue")
@RequiredArgsConstructor
@Tag(name = "Email Queue Management", description = "Monitor and manage the email queue system")
@SecurityRequirement(name = "Bearer Authentication")
public class EmailQueueController {

    private final EmailQueueService emailQueueService;

    @GetMapping("/stats")
    @Operation(
        summary = "Get Email Queue Statistics",
        description = "Retrieve comprehensive statistics about the email queue including pending, sent, and failed counts"
    )
    @ApiResponse(responseCode = "200", description = "Email queue statistics retrieved successfully")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmailQueueService.EmailQueueStats> getEmailQueueStats() {
        EmailQueueService.EmailQueueStats stats = emailQueueService.getQueueStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/cleanup")
    @Operation(
        summary = "Trigger Email Queue Cleanup",
        description = "Manually trigger cleanup of old emails from the queue"
    )
    @ApiResponse(responseCode = "200", description = "Cleanup completed successfully")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> cleanupEmailQueue() {
        emailQueueService.cleanupOldEmails();
        return ResponseEntity.ok("Email queue cleanup completed successfully");
    }

    @PostMapping("/reset-stuck")
    @Operation(
        summary = "Reset Stuck Emails",
        description = "Reset emails that are stuck in processing state back to pending"
    )
    @ApiResponse(responseCode = "200", description = "Stuck emails reset successfully")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> resetStuckEmails() {
        int resetCount = emailQueueService.resetStuckEmails();
        return ResponseEntity.ok(String.format("Reset %d stuck emails back to pending status", resetCount));
    }

    @GetMapping("/health")
    @Operation(
        summary = "Email Queue Health Check",
        description = "Check the health status of the email queue system"
    )
    @ApiResponse(responseCode = "200", description = "Health check completed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmailQueueHealthStatus> getEmailQueueHealth() {
        EmailQueueService.EmailQueueStats stats = emailQueueService.getQueueStats();
        
        EmailQueueHealthStatus health = new EmailQueueHealthStatus();
        health.setOverallStatus(determineOverallHealth(stats));
        health.setStats(stats);
        health.setRecommendations(generateRecommendations(stats));
        
        return ResponseEntity.ok(health);
    }

    private String determineOverallHealth(EmailQueueService.EmailQueueStats stats) {
        // Simple health determination logic
        if (stats.getFailedCount() > 50) {
            return "CRITICAL - High failure rate";
        } else if (stats.getPendingCount() > 100) {
            return "WARNING - High pending count";
        } else if (stats.getProcessingCount() > 20) {
            return "WARNING - Many emails in processing state";
        } else {
            return "HEALTHY";
        }
    }

    private String generateRecommendations(EmailQueueService.EmailQueueStats stats) {
        StringBuilder recommendations = new StringBuilder();
        
        if (stats.getFailedCount() > 10) {
            recommendations.append("• High number of failed emails detected. Check email service configuration. ");
        }
        
        if (stats.getPendingCount() > 50) {
            recommendations.append("• Large number of pending emails. Consider increasing batch size or decreasing processing interval. ");
        }
        
        if (stats.getProcessingCount() > 10) {
            recommendations.append("• Many emails stuck in processing. Consider running reset stuck emails operation. ");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("Email queue is operating normally.");
        }
        
        return recommendations.toString().trim();
    }

    public static class EmailQueueHealthStatus {
        private String overallStatus;
        private EmailQueueService.EmailQueueStats stats;
        private String recommendations;

        // Getters and setters
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        
        public EmailQueueService.EmailQueueStats getStats() { return stats; }
        public void setStats(EmailQueueService.EmailQueueStats stats) { this.stats = stats; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    }
}