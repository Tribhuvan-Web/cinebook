package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.movie.MovieSlotDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotResponseDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.movieService.MovieSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Movie Slot Management", 
     description = "Complete movie slot management including scheduling, theater assignment, and time slot operations")
public class MovieSlotController {

    private final MovieSlotService movieSlotService;

    @PostMapping("/admin/create")
    @Operation(
        summary = "Create movie slot (Admin only)",
        description = "Creates a new movie slot with specified theater, timing, and pricing details. Only accessible by admin users.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Movie slot creation details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MovieSlotDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "movieId": 1,
                        "theaterId": 1,
                        "slotTime": "2024-12-25T19:30:00",
                        "price": 250.0,
                        "availableSeats": 100
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Movie slot created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieSlotResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Movie or theater not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid slot time\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error creating movie slot",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error creating slot: [error details]\""))
        )
    })
    public ResponseEntity<?> createMovieSlot(
        @Parameter(description = "Movie slot creation details", required = true)
        @RequestBody MovieSlotDTO request) {
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