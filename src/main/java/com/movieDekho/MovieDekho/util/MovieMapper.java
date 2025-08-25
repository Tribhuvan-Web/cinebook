package com.movieDekho.MovieDekho.util;

import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MovieMapper {
    
    public static MovieResponseDTO toMovieResponseDTO(AvailableMovie movie) {
        return MovieResponseDTO.fromMovie(movie);
    }
}
