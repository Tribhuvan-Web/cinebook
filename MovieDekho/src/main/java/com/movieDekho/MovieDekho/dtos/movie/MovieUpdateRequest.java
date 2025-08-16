package com.movieDekho.MovieDekho.dtos.movie;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MovieUpdateRequest {
    private String title;
    private LocalDate releaseDate;
    private String duration;
    private String description;
    private String certification;
    private String language;
    private String thumbnail;
    private String trailer;
    private String genre;
    private LocalDate startDate;
    private LocalDate endDate;
}
