package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;
import java.util.Map;

@Data
public class SeatAvailabilityResponse {
    private Long slotId;
    private Map<String, Boolean> seatAvailability; // seat number -> isAvailable
    private String message;
}
