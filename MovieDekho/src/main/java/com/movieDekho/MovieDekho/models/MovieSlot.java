package com.movieDekho.MovieDekho.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// MovieSlot.java (Updated)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovieSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @ToString.Exclude
    private AvailableMovie movie;

    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String theaterName;
    private String screenType;
    private int totalSeats;
    private int availableSeats;
}