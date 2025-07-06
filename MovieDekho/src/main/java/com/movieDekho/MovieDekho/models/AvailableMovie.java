package com.movieDekho.MovieDekho.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AvailableMovie {
    @Id
    private Long id;

    private String title;
    private LocalDate releaseDate;
    private String duration;
    private String description;
    private String certification;
    private String thumbnail;


    private LocalDate startDate;
    private LocalDate endDate;


}