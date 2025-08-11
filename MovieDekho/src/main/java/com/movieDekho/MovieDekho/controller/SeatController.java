package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.seat.SeatRequest;
import com.movieDekho.MovieDekho.dtos.seat.SeatResponse;
import com.movieDekho.MovieDekho.dtos.seat.SeatUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.seatService.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@AllArgsConstructor
@Tag(name = "Seat Management", description = "APIs for managing cinema seats, availability, and pricing")
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/admin/slot/{slotId}")
    @Operation(summary = "Create seats for a movie slot",
            description = "Admin endpoint to create multiple seats for a specific movie slot",
            security = @SecurityRequirement(name = "JWT Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seats created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movie slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid seat data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createSeats(
            @Parameter(description = "ID of the movie slot", required = true)
            @PathVariable Long slotId,
            @Parameter(description = "List of seat details to create", required = true)
            @RequestBody List<SeatRequest> seatRequests) {
        try {
            List<SeatResponse> response = seatService.createSeats(slotId, seatRequests);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating seats: " + e.getMessage());
        }
    }

    @GetMapping("/admin/slot/{slotId}")
    public ResponseEntity<?> getAllSeatsForSlot(@PathVariable Long slotId) {
        try {
            List<SeatResponse> response = seatService.getAllSeatsForSlot(slotId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving seats: " + e.getMessage());
        }
    }

    /**
     * Update seat details (Admin only)
     */
    @PutMapping("/admin/{seatId}")
    public ResponseEntity<?> updateSeat(
            @PathVariable Long seatId,
            @RequestBody SeatUpdateRequest request) {
        try {
            SeatResponse response = seatService.updateSeat(seatId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating seat: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{seatId}")
    public ResponseEntity<?> deleteSeat(@PathVariable Long seatId) {
        try {
            seatService.deleteSeat(seatId);
            return ResponseEntity.ok("Seat deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting seat: " + e.getMessage());
        }
    }

    /**
     * Update seat booking status (Admin only)
     */
    @PutMapping("/admin/{seatId}/booking-status")
    public ResponseEntity<?> updateSeatBookingStatus(
            @PathVariable Long seatId,
            @RequestParam boolean isBooked) {
        try {
            SeatResponse response = seatService.updateSeatBookingStatus(seatId, isBooked);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating seat booking status: " + e.getMessage());
        }
    }

    /**
     * Bulk create seats with pattern (Admin only)
     */
    @PostMapping("/admin/slot/{slotId}/bulk")
    public ResponseEntity<?> bulkCreateSeats(
            @PathVariable Long slotId,
            @RequestParam String rowStart,
            @RequestParam String rowEnd,
            @RequestParam int seatsPerRow,
            @RequestParam double price) {
        try {
            List<SeatResponse> response = seatService.bulkCreateSeats(
                    slotId, rowStart, rowEnd, seatsPerRow, price);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating bulk seats: " + e.getMessage());
        }
    }

    @GetMapping("/slot/{slotId}/available")
    @Operation(summary = "Get available seats for a movie slot",
            description = "Retrieve all available (non-booked) seats for a specific movie slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available seats retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movie slot not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAvailableSeats(
            @Parameter(description = "ID of the movie slot", required = true)
            @PathVariable Long slotId) {
        try {
            List<SeatResponse> response = seatService.getAvailableSeats(slotId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving available seats: " + e.getMessage());
        }
    }

    /**
     * Get booked seats for a slot (User)
     */
    @GetMapping("/slot/{slotId}/booked")
    public ResponseEntity<?> getBookedSeats(@PathVariable Long slotId) {
        try {
            List<SeatResponse> response = seatService.getBookedSeats(slotId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving booked seats: " + e.getMessage());
        }
    }

    /**
     * Get all seats for a slot with booking status (User)
     */
    @GetMapping("/slot/{slotId}")
    public ResponseEntity<?> getAllSeatsWithStatus(@PathVariable Long slotId) {
        try {
            List<SeatResponse> response = seatService.getAllSeatsWithStatus(slotId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving seats: " + e.getMessage());
        }
    }

    /**
     * Get seat by ID (User)
     */
    @GetMapping("/{seatId}")
    @Operation(summary = "Get seat Details from Id"
            , description = "Get all the details from the id either the seat is booked or what is the price")
    public ResponseEntity<?> getSeatById(@PathVariable Long seatId) {
        try {
            SeatResponse response = seatService.getSeatResponseById(seatId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving seat: " + e.getMessage());
        }
    }

    /**
     * Check seat availability (User)
     */
    @GetMapping("/{seatId}/availability")
    @Operation(summary = "Check if a specific seat is available",
            description = "Check the availability status of a specific seat by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat availability checked successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> checkSeatAvailability(
            @Parameter(description = "ID of the seat to check", required = true)
            @PathVariable Long seatId) {
        try {
            SeatService.SeatAvailabilityResponse response = seatService.checkSeatAvailability(seatId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking seat availability: " + e.getMessage());
        }
    }

    /**
     * Get seats by price range (User)
     */
    @GetMapping("/slot/{slotId}/price-range")
    @Operation(summary = "Get seats within a price range",
            description = "Retrieve all seats for a movie slot that fall within the specified price range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seats retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movie slot not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getSeatsByPriceRange(
            @Parameter(description = "ID of the movie slot", required = true)
            @PathVariable Long slotId,
            @Parameter(description = "Minimum price for seat filtering", required = true)
            @RequestParam double minPrice,
            @Parameter(description = "Maximum price for seat filtering", required = true)
            @RequestParam double maxPrice) {
        try {
            List<SeatResponse> response = seatService.getSeatsByPriceRange(slotId, minPrice, maxPrice);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving seats by price range: " + e.getMessage());
        }
    }
}