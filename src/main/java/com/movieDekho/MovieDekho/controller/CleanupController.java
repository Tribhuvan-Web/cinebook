package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.scheduler.TemporarySeatLockCleanupScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cleanup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Cleanup", description = "Administrative cleanup operations")
public class CleanupController {

    private final TemporarySeatLockCleanupScheduler cleanupScheduler;

    @PostMapping("/temporary-locks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Manually trigger cleanup of expired temporary seat locks", 
               description = "Immediately removes all expired temporary seat locks from the database")
    public ResponseEntity<?> cleanupExpiredLocks() {
        try {
            int deletedCount = cleanupScheduler.manualCleanup();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Cleanup completed successfully",
                "deletedCount", deletedCount
            );
            
            log.info("Manual cleanup triggered by admin - Deleted {} locks", deletedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during manual cleanup: ", e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Cleanup failed: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get cleanup system status", 
               description = "Returns information about the cleanup system configuration and status")
    public ResponseEntity<?> getCleanupStatus() {
        Map<String, Object> status = Map.of(
            "cleanupEnabled", true,
            "cleanupInterval", "Every 1 hour",
            "deepCleanupHours", 1,
            "lastCleanupTime", "Check logs for last cleanup time"
        );
        
        return ResponseEntity.ok(status);
    }
}
