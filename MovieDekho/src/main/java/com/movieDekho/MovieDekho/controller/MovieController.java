package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.movieService.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/movies")
@RestController
@AllArgsConstructor
public class MovieController {

    private MovieService movieService;

    @GetMapping("/recent")
    public List<MovieResponseDTO> getRecentMovies() {
        return movieService.getRecentMovies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable Long id) {
        try {
            MovieResponseDTO movie = movieService.getMovieById(id);
            return ResponseEntity.ok(movie);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
