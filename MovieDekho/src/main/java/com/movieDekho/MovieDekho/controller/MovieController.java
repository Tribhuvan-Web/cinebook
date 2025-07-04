package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.service.movieService.MovieService;
import com.movieDekho.MovieDekho.service.userService.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/movies")
@RestController
@AllArgsConstructor
public class MovieController {

    private MovieService movieService;
    private UserService userService;

     @GetMapping("/recent")
    public List<AvailableMovie> getRecentMovies() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return movieService.getRecentMovies();
    }
}
