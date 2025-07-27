package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.seat.SeatRequest;
import com.movieDekho.MovieDekho.dtos.seat.SeatResponse;
import com.movieDekho.MovieDekho.dtos.seat.SeatUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.seatService.SeatService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@AllArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/admin/slot/{slotId}")
    public ResponseEntity<?> createSeats(
            @PathVariable Long slotId,
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

    /**
     * Delete a seat (Admin only)
     */
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

    // ============ USER ENDPOINTS ============

    /**
     * Get available seats for a slot (User)
     */
    @GetMapping("/slot/{slotId}/available")
    public ResponseEntity<?> getAvailableSeats(@PathVariable Long slotId) {
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
    public ResponseEntity<?> checkSeatAvailability(@PathVariable Long seatId) {
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
    public ResponseEntity<?> getSeatsByPriceRange(
            @PathVariable Long slotId,
            @RequestParam double minPrice,
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