package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.booking.BookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.BookingResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import com.movieDekho.MovieDekho.dtos.booking.PaymentBookingRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.service.bookingService.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", 
     description = "Complete booking system for movie tickets including seat selection, payment processing, and booking management")
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtils jwtUtils;

    @PostMapping("/select-seats")
    @Operation(
        summary = "Select seats for booking",
        description = "Temporarily reserve selected seats for a specific movie slot. The seats will be held for a limited time to allow payment completion.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Seat selection details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SeatSelectionRequest.class),
                examples = @ExampleObject(
                    name = "Seat Selection Example",
                    value = """
                    {
                        "slotId": 1,
                        "seatIds": [1, 2, 3],
                        "holdDurationMinutes": 15
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Seats selected successfully",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = SeatSelectionResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "selectionId": "SEL_123456789",
                        "selectedSeats": [
                            {"seatId": 1, "seatNumber": "A1", "price": 150.00},
                            {"seatId": 2, "seatNumber": "A2", "price": 150.00}
                        ],
                        "totalAmount": 300.00,
                        "holdExpiryTime": "2025-08-05T15:30:00",
                        "slotDetails": {
                            "slotId": 1,
                            "movieTitle": "Avengers: Endgame",
                            "showTime": "2025-08-05T14:00:00",
                            "theaterName": "PVR Cinemas"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid or missing authentication token\""))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Slot or seats not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie slot not found\""))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Seats already booked or invalid selection",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Some seats are already booked\""))
        )
    })
    public ResponseEntity<?> selectSeats(
        @Parameter(description = "Seat selection details", required = true)
        @Valid @RequestBody SeatSelectionRequest request,
        @Parameter(description = "JWT token in the format: Bearer <token>", required = true)
        @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            SeatSelectionResponse response = bookingService.selectSeats(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error selecting seats: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error selecting seats: " + e.getMessage());
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<?> createBookingFromSeatSelection(@Valid @RequestBody PaymentBookingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            BookingResponse response = bookingService.createBookingFromSeatSelection(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating booking from seat selection: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating booking: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user email from JWT token
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            BookingResponse response = bookingService.createBooking(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating booking: " + e.getMessage());
        }
    }

    /**
     * Helper method to extract user email from JWT token
     */
    private String extractUserEmailFromToken(String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                if (jwtUtils.validateToken(jwt)) {
                    return jwtUtils.getNameFromJwt(jwt); // This returns email as per JWT configuration
                }
            }
        } catch (Exception e) {
            log.error("Error extracting user from token: ", e);
        }
        return null;
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long bookingId) {
        try {
            BookingResponse response = bookingService.confirmBooking(bookingId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error confirming booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error confirming booking: " + e.getMessage());
        }
    }

    /**
     * Get user's bookings (from JWT token)
     * GET /api/bookings/my-bookings
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            List<BookingResponse> bookings = bookingService.getUserBookings(userEmail);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching user bookings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching bookings: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserBookings(@PathVariable String userEmail) {
        try {
            List<BookingResponse> bookings = bookingService.getUserBookings(userEmail);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching user bookings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching bookings: " + e.getMessage());
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(booking);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching booking: " + e.getMessage());
        }
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            BookingResponse response = bookingService.cancelBooking(bookingId, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error cancelling booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling booking: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{bookingId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelBookingAdmin(
            @PathVariable Long bookingId,
            @RequestParam String userEmail) {
        try {
            BookingResponse response = bookingService.cancelBooking(bookingId, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error cancelling booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling booking: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<BookingResponse> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching all bookings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching bookings: " + e.getMessage());
        }
    }

    @GetMapping("/admin/slot/{slotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingsBySlot(@PathVariable Long slotId) {
        try {
            List<BookingResponse> bookings = bookingService.getBookingsBySlot(slotId);
            return ResponseEntity.ok(bookings);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching slot bookings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching slot bookings: " + e.getMessage());
        }
    }

    @GetMapping("/admin/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingByIdAdmin(@PathVariable Long bookingId) {
        try {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(booking);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching booking: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching booking: " + e.getMessage());
        }
    }
}
