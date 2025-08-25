package com.movieDekho.MovieDekho.exception;

import java.util.List;

public class SeatAlreadySelectedException extends RuntimeException {
    
    private final List<String> conflictingSeats;
    
    public SeatAlreadySelectedException(List<String> conflictingSeats) {
        super(String.format("Seats %s are already selected by other users. Please select different seats.", conflictingSeats));
        this.conflictingSeats = conflictingSeats;
    }
    
    public List<String> getConflictingSeats() {
        return conflictingSeats;
    }
    
    public String getUserFriendlyMessage() {
        if (conflictingSeats.size() == 1) {
            return String.format("Seat %s is already selected by another user. Please choose a different seat.", conflictingSeats.get(0));
        } else {
            return String.format("Seats %s are already selected by other users. Please choose different seats.", conflictingSeats);
        }
    }
}
