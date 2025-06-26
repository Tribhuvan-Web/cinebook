package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.AvailableMovieDTO;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.service.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequestMapping
@RestController
@AllArgsConstructor
public class MovieController {

    private MovieService movieService;

    @PostMapping("/save/movie")
    public ResponseEntity<?> saveMovie(@RequestBody AvailableMovieDTO availableMovieDTO) {
        AvailableMovie availableMovie = new AvailableMovie();
        availableMovie.setTitle(availableMovieDTO.getTitle());
        availableMovie.setDescription(availableMovieDTO.getDescription());
        availableMovie.setDuration(availableMovieDTO.getDuration());
        availableMovie.setThumbnail(availableMovieDTO.getThumbnail());
        availableMovie.setReleaseDate(LocalDate.from(availableMovieDTO.getReleaseDate()));
        movieService.saveMovie(availableMovie);

        return ResponseEntity.ok("Movie saved successfully");
    }
}
