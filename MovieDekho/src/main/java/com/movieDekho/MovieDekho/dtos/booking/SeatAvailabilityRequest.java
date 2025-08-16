package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;
import java.util.List;

@Data
public class SeatAvailabilityRequest {
    private Long slotId;
    private List<String> seatNumbers;
}
