package com.movieDekho.MovieDekho.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

@Entity
@Table(name = "available_movie")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AvailableMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;
    private LocalDate releaseDate;
    private String duration;
    private String description;
    private String certification;
    private String thumbnail;

    private LocalDate startDate;
    private LocalDate endDate;
    private String trailer;
    private String genre;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<MovieSlot> slots = new ArrayList<>();

}