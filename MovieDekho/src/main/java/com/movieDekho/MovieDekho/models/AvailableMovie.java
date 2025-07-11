package com.movieDekho.MovieDekho.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AvailableMovie {

    public AvailableMovie(Long id, String title, LocalDate releaseDate, String duration,
            String description, String certification, String thumbnail,
            LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.description = description;
        this.certification = certification;
        this.thumbnail = thumbnail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.slots = new ArrayList<>();
    }

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

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<MovieSlot> slots = new ArrayList<>();

}