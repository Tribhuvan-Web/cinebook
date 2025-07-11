package com.movieDekho.MovieDekho.service.movieService;

import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieService {

    private MovieRepository movieRepository;

    public List<MovieResponseDTO> getRecentMovies() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return movieRepository.findByReleaseDateAfter(oneYearAgo)
                .stream()
                .map(MovieResponseDTO::fromMovie)
                .collect(Collectors.toList());
    }

    public MovieResponseDTO getMovieById(Long id) {
        AvailableMovie movie = movieRepository.findMovieWithSlotsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return MovieResponseDTO.fromMovie(movie);
    }
}
