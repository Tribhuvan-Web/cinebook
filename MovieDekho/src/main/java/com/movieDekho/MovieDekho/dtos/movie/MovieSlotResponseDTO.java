package com.movieDekho.MovieDekho.dtos.movie;

import com.movieDekho.MovieDekho.models.MovieSlot;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MovieSlotResponseDTO {
    private Long slotId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String theaterName;
    private String screenType;
    private int totalSeats;
    private int availableSeats;

    public static MovieSlotResponseDTO fromMovieSlot(MovieSlot slot) {
        MovieSlotResponseDTO dto = new MovieSlotResponseDTO();
        dto.setSlotId(slot.getSlotId());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setTheaterName(slot.getTheaterName());
        dto.setScreenType(slot.getScreenType());
        dto.setTotalSeats(slot.getTotalSeats());
        dto.setAvailableSeats(slot.getAvailableSeats());
        return dto;
    }
}
