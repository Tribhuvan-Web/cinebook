package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.movie.MovieSlotDTO;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.repository.MovieSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/slots")
@AllArgsConstructor
public class MovieSlotController {

    private final MovieSlotRepository movieSlotRepository;
    private final MovieRepository availableMovieRepository;

    @PostMapping("/save")
    public ResponseEntity<?> createMovieSlot(@RequestBody MovieSlotDTO request) {
        try {
            Optional<AvailableMovie> movieOptional = availableMovieRepository.findById(request.getMovieId());
            if (movieOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Movie not found with ID: " + request.getMovieId());
            }

            AvailableMovie movie = movieOptional.get();

            LocalDate showDate = request.getShowDate();
            if (showDate.isBefore(movie.getStartDate()) || showDate.isAfter(movie.getEndDate())) {
                return ResponseEntity.badRequest().body("Show date must be between " +
                        movie.getStartDate() + " and " + movie.getEndDate());
            }

            MovieSlot slot = new MovieSlot();
            slot.setMovie(movie);
            slot.setShowDate(request.getShowDate());
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
            slot.setTheaterName(request.getTheaterName());
            slot.setScreenType(request.getScreenType());
            slot.setTotalSeats(request.getTotalSeats());
            slot.setAvailableSeats(request.getAvailableSeats());

            MovieSlot savedSlot = movieSlotRepository.save(slot);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSlot);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating slot: " + e.getMessage());
        }
    }
}