package com.movieDekho.MovieDekho.dtos.movie;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvailableMovieDTO {

    private String title;
    private LocalDateTime releaseDate;
    private String duration;
    private String description;
    private String thumbnail;
}
