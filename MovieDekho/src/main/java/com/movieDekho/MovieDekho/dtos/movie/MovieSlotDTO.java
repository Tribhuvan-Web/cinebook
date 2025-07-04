package com.movieDekho.MovieDekho.dtos.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSlotDTO {

    private Long movieId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String theaterName;
    private String screenType;
    private int totalSeats;
    private int availableSeats;
}