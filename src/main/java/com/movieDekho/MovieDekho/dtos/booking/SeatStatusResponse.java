package com.movieDekho.MovieDekho.dtos.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Seat status response showing locked and booked seats for a slot")
public class SeatStatusResponse {
    
    @Schema(description = "Movie slot ID", example = "1")
    private Long slotId;
    
    @Schema(description = "Movie title", example = "Avengers: Endgame")
    private String movieTitle;
    
    @Schema(description = "Cinema name", example = "PVR Cinemas")
    private String cinemaName;
    
    @Schema(description = "Screen type", example = "IMAX")
    private String screenType;
    
    @Schema(description = "Show date", example = "2025-08-17")
    private String showDate;
    
    @Schema(description = "Show time", example = "18:30:00")
    private String showTime;
    
    @Schema(description = "List of all seats with their current status")
    private List<SeatInfo> seats;
    
    @Schema(description = "Summary of seat counts")
    private SeatSummary summary;
    
    @Data
    @NoArgsConstructor
    @Schema(description = "Individual seat information")
    public static class SeatInfo {
        
        @Schema(description = "Seat number", example = "A1")
        private String seatNumber;
        
        @Schema(description = "Seat price", example = "250.0")
        private double price;
        
        @Schema(description = "Current seat status")
        private SeatStatus status;
        
        @Schema(description = "Session ID that has locked this seat (if locked)", example = "sess_12345")
        private String lockedBySession;
        
        @Schema(description = "Lock expiry time in ISO format (if locked)", example = "2025-08-17T19:03:30")
        private String lockExpiresAt;
    }
    
    @Data
    @NoArgsConstructor
    @Schema(description = "Summary of seat availability")
    public static class SeatSummary {
        
        @Schema(description = "Total number of seats", example = "100")
        private int totalSeats;
        
        @Schema(description = "Available seats count", example = "75")
        private int availableSeats;
        
        @Schema(description = "Booked seats count", example = "20")
        private int bookedSeats;
        
        @Schema(description = "Temporarily locked seats count", example = "5")
        private int lockedSeats;
    }
    
    @Schema(description = "Seat status enumeration")
    public enum SeatStatus {
        AVAILABLE("Available for booking"),
        BOOKED("Permanently booked"),
        LOCKED("Temporarily locked by another user"),
        LOCKED_BY_YOU("Temporarily locked by your session");
        
        private final String description;
        
        SeatStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
