package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.service.movieService.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/movies")
@RestController
@AllArgsConstructor
public class MovieController {

    private MovieService movieService;

     @GetMapping("/recent")
    public List<AvailableMovie> getRecentMovies() {
        return movieService.getRecentMovies();
    }

    @GetMapping("/{id}")
    public AvailableMovie getMovieById(@PathVariable Long id) {
         return movieService.getMovieById(id);
    }
}
