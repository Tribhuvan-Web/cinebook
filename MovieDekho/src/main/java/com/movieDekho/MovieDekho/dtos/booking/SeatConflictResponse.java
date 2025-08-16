package com.movieDekho.MovieDekho.dtos.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatConflictResponse {
    private String message;
    private List<String> conflictingSeats;
    private List<String> availableAlternatives;
    private String sessionId;
    private int lockDurationMinutes;
    
    public SeatConflictResponse(String message, List<String> conflictingSeats) {
        this.message = message;
        this.conflictingSeats = conflictingSeats;
        this.lockDurationMinutes = 15;
    }
}
