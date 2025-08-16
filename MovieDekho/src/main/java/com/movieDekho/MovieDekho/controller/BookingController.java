package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.booking.BookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.BookingResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatConflictResponse;
import com.movieDekho.MovieDekho.dtos.booking.PaymentBookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.TicketVerificationDto;
import com.movieDekho.MovieDekho.dtos.booking.VerifyTicketRequest;
import com.movieDekho.MovieDekho.dtos.booking.VerifyTicketResponse;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.exception.SeatAlreadySelectedException;
import com.movieDekho.MovieDekho.service.bookingService.BookingService;
import com.movieDekho.MovieDekho.service.temporarySeatLockService.TemporarySeatLockService;
import com.movieDekho.MovieDekho.service.ticketVerificationService.TicketVerificationService;
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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", 
     description = "Complete booking system for movie tickets including seat selection, payment processing, and booking management")
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtils jwtUtils;
    private final TemporarySeatLockService temporarySeatLockService;
    private final TicketVerificationService ticketVerificationService;

    @PostMapping("/seats/check-availability")
    @Operation(
        summary = "Check seat availability (Public)",
        description = "Check if seats are available for selection. No authentication required. This endpoint is used by frontend to verify seat availability before proceeding to payment.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Seat availability check details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SeatAvailabilityRequest.class),
                examples = @ExampleObject(
                    name = "Seat Availability Check Example",
                    value = """
                    {
                        "slotId": 1,
                        "seatNumbers": ["A1", "A2", "A3"]
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Seat availability checked successfully",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = SeatAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "slotId": 1,
                        "seatAvailability": {
                            "A1": true,
                            "A2": true,
                            "A3": false
                        },
                        "message": "Seat availability checked successfully"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Slot not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie slot not found\""))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid seat numbers provided\""))
        )
    })
    public ResponseEntity<?> checkSeatAvailability(
        @Parameter(description = "Seat availability check details", required = true)
        @Valid @RequestBody SeatAvailabilityRequest request) {
        try {
            SeatAvailabilityResponse response = bookingService.checkSeatAvailability(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error checking seat availability: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking seat availability: " + e.getMessage());
        }
    }

    @PostMapping("/select-seats")
    @Operation(
        summary = "Select seats for booking (Public - No Auth Required)",
        description = "Select seats for a specific movie slot. This endpoint is now public to reduce backend load. Frontend manages seat selection state. Authentication is only required at payment time.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Seat selection details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SeatSelectionRequest.class),
                examples = @ExampleObject(
                    name = "Seat Selection Example (Public)",
                    value = """
                    {
                        "slotId": 1,
                        "seatNumbers": ["A1", "A2", "A3"]
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Seat information retrieved successfully (for frontend storage)",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = SeatSelectionResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "slotId": 1,
                        "seatNumbers": ["A1", "A2", "A3"],
                        "totalAmount": 450.00,
                        "movieTitle": "Avengers: Endgame",
                        "cinemaName": "PVR Cinemas",
                        "showTime": "14:00:00",
                        "showDate": "2025-08-05",
                        "seatDetails": [
                            {"seatNumber": "A1", "price": 150.00},
                            {"seatNumber": "A2", "price": 150.00},
                            {"seatNumber": "A3", "price": 150.00}
                        ]
                    }
                    """
                )
            )
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
        @Parameter(description = "Session ID for temporary seat locking (optional)")
        @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
        @Parameter(description = "User identifier for session generation (optional)")
        @RequestHeader(value = "X-User-ID", required = false) String userIdentifier) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = temporarySeatLockService.generateSessionId(userIdentifier);
                log.info("Generated new session ID: {} for user: {}", sessionId, userIdentifier);
            }
            
            SeatSelectionResponse response = bookingService.getSeatsInformation(request, sessionId);
            
            // Add session ID to response headers for frontend to use
            return ResponseEntity.ok()
                    .header("X-Session-ID", sessionId)
                    .header("X-Lock-Duration", "03")  // 03 minutes lock duration
                    .body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Movie slot or seats not found. Please check your selection and try again.");
        } catch (SeatAlreadySelectedException e) {
            // Handle seat conflict with structured response
            log.warn("Seat selection conflict for session {}: {}", sessionId, e.getConflictingSeats());
            
            SeatConflictResponse conflictResponse = new SeatConflictResponse(
                e.getUserFriendlyMessage(),
                e.getConflictingSeats()
            );
            conflictResponse.setSessionId(sessionId);
            
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(conflictResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid seat selection: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already selected by other users")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            log.error("Unexpected error during seat selection: ", e);
            return ResponseEntity.badRequest()
                    .body("Unable to select seats. Please try again.");
        } catch (Exception e) {
            log.error("Error getting seat information: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Service temporarily unavailable. Please try again later.");
        }
    }

    @DeleteMapping("/release-seats")
    @Operation(
        summary = "Release temporarily locked seats (Public)",
        description = "Release seats that were temporarily locked during selection. This should be called when user navigates away or changes seat selection.",
        parameters = @Parameter(
            name = "X-Session-ID",
            description = "Session ID to release locks for",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seats released successfully"),
        @ApiResponse(responseCode = "400", description = "Session ID is required")
    })
    public ResponseEntity<?> releaseSeatLocks(
        @Parameter(description = "Session ID for seat lock release", required = true)
        @RequestHeader(value = "X-Session-ID") String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Session ID is required");
            }
            
            temporarySeatLockService.releaseLocksBySession(sessionId);
            log.info("Released seat locks for session: {}", sessionId);
            
            return ResponseEntity.ok().body("Seat locks released successfully");
        } catch (Exception e) {
            log.error("Error releasing seat locks: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error releasing seat locks: " + e.getMessage());
        }
    }

    @PostMapping("/payment")
    @Operation(
        summary = "Process payment and create booking (Auth Required)",
        description = "Process payment for selected seats and create booking. This endpoint includes race condition handling with pessimistic locking to ensure seat booking consistency. Frontend should send selected seats data along with payment details.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payment and booking details including selected seats",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentBookingRequest.class),
                examples = @ExampleObject(
                    name = "Payment Request Example",
                    value = """
                    {
                        "slotId": 1,
                        "seatNumbers": ["A1", "A2", "A3"],
                        "totalAmount": 450.00,
                        "paymentMethod": "CARD",
                        "cardNumber": "1234567890123456",
                        "cardHolderName": "John Doe",
                        "expiryDate": "12/25",
                        "cvv": "123"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Booking created successfully with race condition protection",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = BookingResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid or missing authentication token\""))
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Seats no longer available (Race condition handled)",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Seats no longer available: [A1, A2]\""))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Payment failed or invalid request",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Payment failed: Insufficient funds\""))
        )
    })
    public ResponseEntity<?> createBookingFromSeatSelection(@Valid @RequestBody PaymentBookingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            
            // Race condition handling happens inside the service method
            BookingResponse response = bookingService.createBookingWithRaceConditionHandling(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle seat availability conflicts
            if (e.getMessage().contains("no longer available") || e.getMessage().contains("already booked")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
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
    
    @PostMapping("/admin/fix-null-seat-numbers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Fix null seat numbers in existing bookings (Admin only)",
        description = "Administrative endpoint to fix bookings that have null seat numbers"
    )
    public ResponseEntity<?> fixNullSeatNumbers() {
        try {
            bookingService.fixNullSeatNumbers();
            return ResponseEntity.ok("Successfully fixed bookings with null seat numbers");
        } catch (Exception e) {
            log.error("Error fixing null seat numbers: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fixing null seat numbers: " + e.getMessage());
        }
    }

    // Ticket Verification Endpoints

    @GetMapping("/{bookingId}/ticket")
    @Operation(
        summary = "Get ticket verification details (User Panel)",
        description = "Get ticket QR code and verification details for a specific booking. Returns the QR code that can be used for verification.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Ticket details retrieved successfully",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = TicketVerificationDto.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "verificationToken": "YWJjZGVmZ2hpams",
                        "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4",
                        "bookingId": 123,
                        "movieTitle": "Avengers: Endgame",
                        "cinemaName": "PVR Cinemas",
                        "showDate": "2025-08-05",
                        "showTime": "14:00:00",
                        "seatNumbers": "A1, A2, A3",
                        "userEmail": "user@example.com",
                        "verified": false,
                        "verificationTime": null,
                        "verifiedBy": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid or missing authentication token\""))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Booking not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Booking not found\""))
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Access denied - Not your booking",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
        )
    })
    public ResponseEntity<?> getTicketVerificationDetails(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userEmail = extractUserEmailFromToken(authHeader);
            if (userEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            // Verify that the booking belongs to the user
            BookingResponse booking = bookingService.getBookingById(bookingId);
            if (!userEmail.equals(booking.getUserEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            TicketVerificationDto ticketDetails = ticketVerificationService.getTicketVerificationDetails(bookingId);
            return ResponseEntity.ok(ticketDetails);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching ticket details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching ticket details: " + e.getMessage());
        }
    }

    @PostMapping("/admin/verify-ticket")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Verify ticket using QR code (Admin Only)",
        description = "Admin endpoint to verify a ticket using QR code. This marks the ticket as verified and records verification details.",
        security = @SecurityRequirement(name = "JWT Authentication"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "QR code verification request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VerifyTicketRequest.class),
                examples = @ExampleObject(
                    name = "Verify Ticket Request",
                    value = """
                    {
                        "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Ticket verification completed",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = VerifyTicketResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "valid": true,
                        "message": "Ticket verified successfully",
                        "ticketDetails": {
                            "bookingId": 123,
                            "movieTitle": "Avengers: Endgame",
                            "cinemaName": "PVR Cinemas",
                            "showDate": "2025-08-05",
                            "showTime": "14:00:00",
                            "seatNumbers": "A1, A2, A3",
                            "userEmail": "user@example.com",
                            "verified": true
                        },
                        "verificationTime": "2025-08-16 10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid QR code or ticket not found",
            content = @Content(
                mediaType = "application/json", 
                examples = @ExampleObject(value = """
                    {
                        "valid": false,
                        "message": "Invalid ticket QR code"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Admin access required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin access required\""))
        )
    })
    public ResponseEntity<?> verifyTicket(
            @Valid @RequestBody VerifyTicketRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String adminEmail = extractUserEmailFromToken(authHeader);
            if (adminEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            VerifyTicketResponse response = ticketVerificationService.verifyTicket(request.getQrCode(), adminEmail);
            
            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error verifying ticket: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VerifyTicketResponse(false, "Error verifying ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/slot/{slotId}/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all booking tickets for a show (Admin Panel)",
        description = "Admin endpoint to view all bookings with QR codes for a specific movie slot/show. This helps admins see who has booked tickets and their verification status.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Booking tickets retrieved successfully",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(type = "array", implementation = TicketVerificationDto.class),
                examples = @ExampleObject(
                    value = """
                    [
                        {
                            "verificationToken": "YWJjZGVmZ2hpams",
                            "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4",
                            "bookingId": 123,
                            "movieTitle": "Avengers: Endgame",
                            "cinemaName": "PVR Cinemas",
                            "showDate": "2025-08-05",
                            "showTime": "14:00:00",
                            "seatNumbers": "A1, A2, A3",
                            "userEmail": "user@example.com",
                            "verified": false,
                            "verificationTime": null,
                            "verifiedBy": null
                        },
                        {
                            "verificationToken": "ZGVmZ2hpamts",
                            "qrCode": "ZGVmZ2hpamts:B5C6D7E8",
                            "bookingId": 124,
                            "movieTitle": "Avengers: Endgame",
                            "cinemaName": "PVR Cinemas",
                            "showDate": "2025-08-05",
                            "showTime": "14:00:00",
                            "seatNumbers": "B1, B2",
                            "userEmail": "user2@example.com",
                            "verified": true,
                            "verificationTime": "2025-08-05 13:45:00",
                            "verifiedBy": "admin@cinema.com"
                        }
                    ]
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Movie slot not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie slot not found\""))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Admin access required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin access required\""))
        )
    })
    public ResponseEntity<?> getAllTicketsForShow(
            @PathVariable Long slotId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String adminEmail = extractUserEmailFromToken(authHeader);
            if (adminEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            // Get all bookings for the slot
            List<BookingResponse> bookings = bookingService.getBookingsBySlot(slotId);
            
            // Convert to ticket verification DTOs with QR codes
            List<TicketVerificationDto> tickets = bookings.stream()
                    .map(booking -> {
                        try {
                            return ticketVerificationService.getTicketVerificationDetails(booking.getBookingId());
                        } catch (Exception e) {
                            log.warn("Could not get verification details for booking {}: {}", booking.getBookingId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(ticket -> ticket != null)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tickets);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching tickets for show: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching tickets: " + e.getMessage());
        }
    }

    @GetMapping("/admin/cinema/{cinemaName}/today-tickets")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all today's tickets for a cinema (Admin Panel)",
        description = "Admin endpoint to view all bookings with QR codes for today's shows at a specific cinema. Useful for daily ticket management.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Today's tickets retrieved successfully",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(type = "array", implementation = TicketVerificationDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Admin access required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin access required\""))
        )
    })
    public ResponseEntity<?> getTodayTicketsForCinema(
            @PathVariable String cinemaName,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String adminEmail = extractUserEmailFromToken(authHeader);
            if (adminEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            // Get today's tickets for the cinema
            List<TicketVerificationDto> todayTickets = bookingService.getTodayTicketsForCinema(cinemaName);

            return ResponseEntity.ok(todayTickets);
        } catch (Exception e) {
            log.error("Error fetching today's tickets for cinema: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching today's tickets: " + e.getMessage());
        }
    }

    @GetMapping("/admin/verification-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get verification summary statistics (Admin Panel)",
        description = "Admin endpoint to view verification statistics - total tickets, verified tickets, pending verification, etc.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Verification summary retrieved successfully",
            content = @Content(
                mediaType = "application/json", 
                examples = @ExampleObject(
                    value = """
                    {
                        "totalTickets": 150,
                        "verifiedTickets": 89,
                        "pendingVerification": 61,
                        "verificationRate": 59.33,
                        "todayVerifications": 25,
                        "recentVerifications": [
                            {
                                "bookingId": 123,
                                "movieTitle": "Avengers: Endgame",
                                "userEmail": "user@example.com",
                                "verificationTime": "2025-08-16 10:30:00",
                                "verifiedBy": "admin@cinema.com"
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Admin access required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin access required\""))
        )
    })
    public ResponseEntity<?> getVerificationSummary(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String adminEmail = extractUserEmailFromToken(authHeader);
            if (adminEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }

            Map<String, Object> summary = bookingService.getVerificationSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching verification summary: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching verification summary: " + e.getMessage());
        }
    }
}
