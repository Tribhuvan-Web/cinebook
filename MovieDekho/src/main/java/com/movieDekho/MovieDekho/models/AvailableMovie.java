package com.movieDekho.MovieDekho.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// AvailableMovie.java (Updated)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableMovie {
    @Id
    private Long id;

    private String title;
    private LocalDate releaseDate;
    private String duration;
    private String description;
    private String certification;
    private String thumbnail;

    // Add these fields
    private LocalDate startDate; // First available booking date
    private LocalDate endDate;   // Last available booking date (min startDate+4 days)
}