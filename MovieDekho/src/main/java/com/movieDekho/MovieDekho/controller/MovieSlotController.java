package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.movie.MovieSlotDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotResponseDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.movieService.MovieSlotService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@AllArgsConstructor
public class MovieSlotController {

    private final MovieSlotService movieSlotService;

    @PostMapping("/admin/create")
    public ResponseEntity<?> createMovieSlot(@RequestBody MovieSlotDTO request) {
        try {
            MovieSlotResponseDTO response = movieSlotService.createMovieSlot(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating slot: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<MovieSlotResponseDTO>> getAllSlots() {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getAllSlots();
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PutMapping("/admin/{slotId}")
    public ResponseEntity<?> updateMovieSlot(@PathVariable Long slotId, @RequestBody MovieSlotUpdateRequest request) {
        try {
            MovieSlotResponseDTO response = movieSlotService.updateMovieSlot(slotId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating slot: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{slotId}")
    public ResponseEntity<?> deleteMovieSlot(@PathVariable Long slotId) {
        try {
            movieSlotService.deleteMovieSlot(slotId);
            return ResponseEntity.ok("Movie slot deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting slot: " + e.getMessage());
        }
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<MovieSlotResponseDTO>> getSlotsByMovie(@PathVariable Long movieId) {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getSlotsByMovie(movieId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/movie/{movieId}/date/{date}")
    public ResponseEntity<List<MovieSlotResponseDTO>> getSlotsByMovieAndDate(
            @PathVariable Long movieId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getSlotsByMovieAndDate(movieId, date);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<?> getSlotById(@PathVariable Long slotId) {
        try {
            MovieSlotResponseDTO response = movieSlotService.getSlotById(slotId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving slot: " + e.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<MovieSlotResponseDTO>> getAvailableSlots() {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getAvailableSlots();
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/theater/{theaterName}")
    public ResponseEntity<List<MovieSlotResponseDTO>> getSlotsByTheater(@PathVariable String theaterName) {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getSlotsByTheater(theaterName);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<MovieSlotResponseDTO>> getSlotsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.getSlotsByDateRange(startDate, endDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSlotResponseDTO>> searchSlots(
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String theaterName) {
        try {
            List<MovieSlotResponseDTO> slots = movieSlotService.searchSlots(movieId, date, theaterName);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}