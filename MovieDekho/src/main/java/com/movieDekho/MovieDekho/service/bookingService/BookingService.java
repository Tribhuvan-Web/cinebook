package com.movieDekho.MovieDekho.service.bookingService;

import com.movieDekho.MovieDekho.dtos.booking.BookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.BookingResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityResponse;
import com.movieDekho.MovieDekho.dtos.booking.PaymentBookingRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.exception.SeatAlreadySelectedException;
import com.movieDekho.MovieDekho.models.*;
import com.movieDekho.MovieDekho.repository.*;
import com.movieDekho.MovieDekho.service.paymentService.MockPaymentService;
import com.movieDekho.MovieDekho.service.temporarySeatLockService.TemporarySeatLockService;
import com.movieDekho.MovieDekho.service.ticketVerificationService.TicketVerificationService;
import com.movieDekho.MovieDekho.dtos.booking.TicketVerificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MovieSlotRepository movieSlotRepository;
    private final SeatRepository seatRepository;
    private final MockPaymentService mockPaymentService;
    private final ObjectMapper objectMapper;
    private final SeatSelectionCacheService seatSelectionCacheService;
    private final TemporarySeatLockService temporarySeatLockService;
    private final TicketVerificationService ticketVerificationService;

    public SeatSelectionResponse getSeatsInformation(SeatSelectionRequest request) {
        return getSeatsInformation(request, null);
    }

    /**
     * NEW METHOD: Get seat information with session-based temporary locking
     * Used for frontend seat selection management
     */
    public SeatSelectionResponse getSeatsInformation(SeatSelectionRequest request, String sessionId) {
        try {
            MovieSlot slot = movieSlotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + request.getSlotId()));

            List<Seat> seats = validateAndGetSeatsBySeatNumbers(request.getSeatNumbers(), slot);

            // Check if seats are available (but don't reserve them)
            checkSeatAvailability(seats, slot);

            // Check for temporary locks by other sessions
            if (sessionId != null) {
                List<String> unavailableSeats = temporarySeatLockService.lockSeats(seats, sessionId);
                if (!unavailableSeats.isEmpty()) {
                    throw new SeatAlreadySelectedException(unavailableSeats);
                }
            }

            double seatAmount = seats.stream().mapToDouble(Seat::getPrice).sum();
            double basePrice = (seatAmount * 7) / 100;
            double taxPrice = basePrice + ((basePrice * 18) / 100);
            double totalAmount = Math.round((taxPrice) * 100.0) / 100.0 + seatAmount;

            SeatSelectionResponse response = new SeatSelectionResponse();
            response.setSlotId(slot.getSlotId());
            response.setSeatNumbers(request.getSeatNumbers());
            response.setTicketFee(seatAmount);
            response.setConvenienceFee(taxPrice);
            response.setTotalAmount(totalAmount);
            response.setMovieTitle(slot.getMovie().getTitle());
            response.setMovieDescription(slot.getMovie().getDescription());
            response.setCinemaName(slot.getTheaterName());
            response.setScreenName(slot.getScreenType());
            response.setShowTime(slot.getStartTime().toString());
            response.setShowDate(slot.getShowDate().toString());

            List<SeatSelectionResponse.SeatDetails> seatDetailsList = seats.stream()
                    .map(seat -> {
                        SeatSelectionResponse.SeatDetails seatDetails = new SeatSelectionResponse.SeatDetails();
                        seatDetails.setSeatNumber(seat.getSeatNumber());
                        seatDetails.setPrice(seat.getPrice());
                        return seatDetails;
                    })
                    .collect(Collectors.toList());
            response.setSeatDetails(seatDetailsList);

            log.info("Seat information retrieved successfully - Seats: {} - Total: {} - Session: {}",
                    request.getSeatNumbers(), totalAmount, sessionId);

            // NO CACHING - Frontend will handle seat selection state
            return response;

        } catch (Exception e) {
            if (e instanceof SeatAlreadySelectedException) {
                // Don't log seat conflicts as errors - they're expected behavior
                log.info("Seat selection conflict: {}", e.getMessage());
            } else {
                log.error("Failed to get seat information: ", e);
            }
            throw new RuntimeException("Failed to get seat information: " + e.getMessage());
        }
    }

    /**
     * NEW METHOD: Check seat availability without authentication
     * Used for real-time seat availability checking
     */
    public SeatAvailabilityResponse checkSeatAvailability(SeatAvailabilityRequest request) {
        try {
            MovieSlot slot = movieSlotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + request.getSlotId()));

            List<Seat> seats = validateAndGetSeatsBySeatNumbers(request.getSeatNumbers(), slot);

            Map<String, Boolean> availability = new HashMap<>();
            for (Seat seat : seats) {
                boolean isAvailable = !seat.isBooked()
                        && !bookingRepository.isSeatBookedForSlot(slot, seat.getSeatNumber());
                availability.put(seat.getSeatNumber(), isAvailable);
            }

            SeatAvailabilityResponse response = new SeatAvailabilityResponse();
            response.setSlotId(slot.getSlotId());
            response.setSeatAvailability(availability);
            response.setMessage("Seat availability checked successfully");

            log.info("Seat availability checked for seats: {} in slot: {}", request.getSeatNumbers(),
                    request.getSlotId());
            return response;

        } catch (Exception e) {
            log.error("Failed to check seat availability: ", e);
            throw new RuntimeException("Failed to check seat availability: " + e.getMessage());
        }
    }

    /**
     * NEW METHOD: Create booking with race condition handling
     * Uses pessimistic locking to prevent concurrent seat booking conflicts
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse createBookingWithRaceConditionHandling(PaymentBookingRequest paymentRequest,
            String userEmail) {
        try {
            log.info("Starting booking creation with race condition handling for user: {} - Seats: {}",
                    userEmail, paymentRequest.getSeatNumbers());

            // Validate user
            User user = userRepository.findByEmailOrPhone(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

            // Validate movie slot
            MovieSlot slot = movieSlotRepository.findById(paymentRequest.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + paymentRequest.getSlotId()));

            // RACE CONDITION PROTECTION: Use pessimistic locking to lock seats
            List<Seat> seats = seatRepository.findBySlotAndSeatNumbersWithLock(slot, paymentRequest.getSeatNumbers());

            if (seats.size() != paymentRequest.getSeatNumbers().size()) {
                List<String> foundSeatNumbers = seats.stream()
                        .map(Seat::getSeatNumber)
                        .collect(Collectors.toList());
                List<String> missingSeatNumbers = paymentRequest.getSeatNumbers().stream()
                        .filter(seatNumber -> !foundSeatNumbers.contains(seatNumber))
                        .collect(Collectors.toList());
                throw new ResourceNotFoundException("Seats not found: " + missingSeatNumbers);
            }

            // Check seat availability within the locked transaction
            List<String> unavailableSeats = seats.stream()
                    .filter(seat -> seat.isBooked()
                            || bookingRepository.isSeatBookedForSlot(slot, seat.getSeatNumber()))
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.toList());

            if (!unavailableSeats.isEmpty()) {
                throw new IllegalArgumentException("Seats no longer available: " + unavailableSeats);
            }

            // Calculate and validate total amount
            double calculatedTotal = seats.stream().mapToDouble(Seat::getPrice).sum();
            if (Math.abs(calculatedTotal - paymentRequest.getTotalAmount()) > 0.01) {
                throw new IllegalArgumentException("Total amount mismatch. Expected: " + calculatedTotal
                        + ", Provided: " + paymentRequest.getTotalAmount());
            }

            // Process payment
            MockPaymentService.PaymentResponse paymentResponse = mockPaymentService.processPayment(
                    calculatedTotal,
                    paymentRequest.getPaymentMethod(),
                    paymentRequest.getCardNumber(),
                    paymentRequest.getCardHolderName(),
                    paymentRequest.getExpiryDate(),
                    paymentRequest.getCvv());

            if (!"SUCCESS".equals(paymentResponse.getStatus())) {
                throw new IllegalArgumentException("Payment failed: " + paymentResponse.getMessage());
            }

            // ATOMICALLY mark seats as booked
            for (Seat seat : seats) {
                seat.setBooked(true);
            }
            seatRepository.saveAll(seats);

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSlot(slot);
            booking.setUserEmail(userEmail);
            booking.setTotalAmount(calculatedTotal);
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentMethod(paymentRequest.getPaymentMethod());
            booking.setSeatNumbers(paymentRequest.getSeatNumbers());
            booking.setPaymentStatus("COMPLETED");
            booking.setPaymentId(paymentResponse.getPaymentId());

            // Store payment details as JSON
            try {
                String paymentDetailsJson = objectMapper.writeValueAsString(paymentResponse.getPaymentDetails());
                booking.setPaymentDetails(paymentDetailsJson);
            } catch (Exception e) {
                log.warn("Failed to serialize payment details: ", e);
            }

            // Update available seats in slot
            updateSlotAvailableSeats(slot, paymentRequest.getSeatNumbers().size());

            // Save booking
            booking = bookingRepository.save(booking);

            // Generate verification data for confirmed booking
            ticketVerificationService.generateVerificationDataForBooking(booking.getBookingId());

            log.info("Booking created successfully with race condition protection: {} - Seats: {}",
                    booking.getBookingId(), paymentRequest.getSeatNumbers());

            return convertToBookingResponse(booking);

        } catch (Exception e) {
            log.error("Booking creation with race condition handling failed: ", e);
            throw new RuntimeException("Booking creation failed: " + e.getMessage());
        }
    }

    /**
     * Create booking using cached seat selection data
     */
    @Transactional
    public BookingResponse createBookingFromSeatSelection(PaymentBookingRequest paymentRequest, String userEmail) {
        try {
            // Get cached seat selection
            SeatSelectionResponse seatSelection = seatSelectionCacheService.getSeatSelection(userEmail,
                    paymentRequest.getSlotId());
            if (seatSelection == null) {
                throw new IllegalArgumentException(
                        "No seat selection found. Please select seats first using /select-seats endpoint.");
            }

            // Validate user
            User user = userRepository.findByEmailOrPhone(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

            // Validate movie slot
            MovieSlot slot = movieSlotRepository.findById(paymentRequest.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + paymentRequest.getSlotId()));

            // Validate seats by seat numbers (from cached selection)
            List<Seat> seats = validateAndGetSeatsBySeatNumbers(seatSelection.getSeatNumbers(), slot);

            // Check if seats are still available
            checkSeatAvailability(seats, slot);

            // Use total amount from cached selection
            double totalAmount = seatSelection.getTotalAmount();

            // Process payment
            MockPaymentService.PaymentResponse paymentResponse = mockPaymentService.processPayment(
                    totalAmount,
                    paymentRequest.getPaymentMethod(),
                    paymentRequest.getCardNumber(),
                    paymentRequest.getCardHolderName(),
                    paymentRequest.getExpiryDate(),
                    paymentRequest.getCvv());

            if (!"SUCCESS".equals(paymentResponse.getStatus())) {
                throw new IllegalArgumentException("Payment failed: " + paymentResponse.getMessage());
            }

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSlot(slot);
            booking.setUserEmail(userEmail);
            booking.setTotalAmount(totalAmount);
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentMethod(paymentRequest.getPaymentMethod());
            booking.setSeatNumbers(seatSelection.getSeatNumbers());
            booking.setPaymentStatus("COMPLETED");
            booking.setPaymentId(paymentResponse.getPaymentId());

            // Store payment details as JSON
            try {
                String paymentDetailsJson = objectMapper.writeValueAsString(paymentResponse.getPaymentDetails());
                booking.setPaymentDetails(paymentDetailsJson);
            } catch (Exception e) {
                log.warn("Failed to serialize payment details: ", e);
            }

            // Mark seats as booked immediately
            markSeatsAsBooked(seats);

            // Update available seats in slot
            updateSlotAvailableSeats(slot, seatSelection.getSeatNumbers().size());

            // Save booking
            booking = bookingRepository.save(booking);

            // Generate verification data for confirmed booking
            ticketVerificationService.generateVerificationDataForBooking(booking.getBookingId());

            // Remove seat selection from cache after successful booking
            seatSelectionCacheService.removeSeatSelection(userEmail, paymentRequest.getSlotId());

            log.info("Booking created successfully from cached seat selection: {}", booking.getBookingId());
            return convertToBookingResponse(booking);

        } catch (Exception e) {
            log.error("Booking creation from seat selection failed: ", e);
            throw new RuntimeException("Booking creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        try {
            // Validate user
            User user = userRepository.findByEmailOrPhone(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

            // Validate movie slot
            MovieSlot slot = movieSlotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + request.getSlotId()));

            // Validate seats by seat numbers
            List<Seat> seats = validateAndGetSeatsBySeatNumbers(request.getSeatNumbers(), slot);

            // Check if seats are available
            checkSeatAvailability(seats, slot);

            // Calculate total amount
            double calculatedTotal = seats.stream().mapToDouble(Seat::getPrice).sum();
            if (Math.abs(calculatedTotal - request.getTotalAmount()) > 0.01) {
                throw new IllegalArgumentException("Total amount mismatch. Expected: " + calculatedTotal
                        + ", Provided: " + request.getTotalAmount());
            }

            // Process payment
            MockPaymentService.PaymentResponse paymentResponse = mockPaymentService.processPayment(
                    calculatedTotal,
                    request.getPaymentMethod(),
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv());

            if (!"SUCCESS".equals(paymentResponse.getStatus())) {
                throw new IllegalArgumentException("Payment failed: " + paymentResponse.getMessage());
            }

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSlot(slot);
            booking.setUserEmail(userEmail);
            booking.setTotalAmount(calculatedTotal);
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.CONFIRMED); // Directly confirm since payment is successful
            booking.setPaymentMethod(request.getPaymentMethod());
            booking.setSeatNumbers(request.getSeatNumbers());
            booking.setPaymentStatus("COMPLETED");
            booking.setPaymentId(paymentResponse.getPaymentId());

            // Store payment details as JSON
            try {
                String paymentDetailsJson = objectMapper.writeValueAsString(paymentResponse.getPaymentDetails());
                booking.setPaymentDetails(paymentDetailsJson);
            } catch (Exception e) {
                log.warn("Failed to serialize payment details: ", e);
            }

            // Mark seats as booked immediately
            markSeatsAsBooked(seats);

            // Update available seats in slot
            updateSlotAvailableSeats(slot, request.getSeatNumbers().size());

            // Save booking
            booking = bookingRepository.save(booking);

            log.info("Booking created and confirmed successfully: {}", booking.getBookingId());
            return convertToBookingResponse(booking);

        } catch (Exception e) {
            log.error("Booking creation failed: ", e);
            throw new RuntimeException("Booking creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        try {
            // Find booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

            // Update booking status
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus("COMPLETED");

            // Mark seats as booked
            markSeatsAsBooked(booking);

            // Update available seats in slot
            updateSlotAvailableSeats(booking.getSlot(), booking.getSeatNumbers().size());

            booking = bookingRepository.save(booking);

            // Generate verification data for confirmed booking
            ticketVerificationService.generateVerificationDataForBooking(booking.getBookingId());

            log.info("Booking confirmed successfully: {}", booking.getBookingId());
            return convertToBookingResponse(booking);

        } catch (Exception e) {
            log.error("Booking confirmation failed: ", e);
            throw new RuntimeException("Booking confirmation failed: " + e.getMessage());
        }
    }

    private void markSeatsAsBooked(List<Seat> seats) {
        for (Seat seat : seats) {
            seat.setBooked(true);
        }
        seatRepository.saveAll(seats);
    }

    private void markSeatsAsBooked(Booking booking) {
        List<Seat> seats = seatRepository.findBySlotAndSeatNumberIn(
                booking.getSlot(),
                booking.getSeatNumbers());
        markSeatsAsBooked(seats);
    }

    private void updateSlotAvailableSeats(MovieSlot slot, int bookedSeatsCount) {
        slot.setAvailableSeats(slot.getAvailableSeats() - bookedSeatsCount);
        movieSlotRepository.save(slot);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        List<Booking> bookings = bookingRepository.findByUserEmailWithDetails(userEmail);
        return bookings.stream()
                .map(booking -> convertToBookingResponse(booking))
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return convertToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Verify user owns the booking
        if (!booking.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        // Process refund if payment was successful
        if ("COMPLETED".equals(booking.getPaymentStatus()) && booking.getPaymentId() != null) {
            try {
                MockPaymentService.PaymentResponse refundResponse = mockPaymentService.refundPayment(
                        booking.getPaymentId(),
                        booking.getTotalAmount());
                log.info("Refund processed: {}", refundResponse.getPaymentId());

                // Update payment status to indicate refund
                booking.setPaymentStatus("REFUNDED");

                // Store refund details
                try {
                    String refundDetailsJson = objectMapper.writeValueAsString(refundResponse);
                    booking.setPaymentDetails(booking.getPaymentDetails() + "\n\nREFUND: " + refundDetailsJson);
                } catch (Exception e) {
                    log.warn("Failed to serialize refund details: ", e);
                }

            } catch (Exception e) {
                log.error("Error processing refund: ", e);
                // Continue with cancellation even if refund fails
            }
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Release seats
        releaseSeats(booking);

        // Update slot available seats
        updateSlotAvailableSeats(booking.getSlot(), -booking.getSeatNumbers().size());

        booking = bookingRepository.save(booking);
        return convertToBookingResponse(booking);
    }

    private void releaseSeats(Booking booking) {
        List<Seat> seats = seatRepository.findBySlotAndSeatNumberIn(
                booking.getSlot(),
                booking.getSeatNumbers());

        for (Seat seat : seats) {
            seat.setBooked(false);
        }

        seatRepository.saveAll(seats);
    }

    private List<Seat> validateAndGetSeatsBySeatNumbers(List<String> seatNumbers, MovieSlot slot) {
        List<Seat> seats = seatRepository.findBySlotAndSeatNumberIn(slot, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            List<String> foundSeatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.toList());
            List<String> missingSeatNumbers = seatNumbers.stream()
                    .filter(seatNumber -> !foundSeatNumbers.contains(seatNumber))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Seats not found: " + missingSeatNumbers);
        }

        return seats;
    }

    private void checkSeatAvailability(List<Seat> seats, MovieSlot slot) {
        for (Seat seat : seats) {
            if (seat.isBooked()) {
                throw new IllegalArgumentException("Seat " + seat.getSeatNumber() + " is already booked");
            }

            boolean isAlreadyBooked = bookingRepository.isSeatBookedForSlot(slot, seat.getSeatNumber());
            if (isAlreadyBooked) {
                throw new IllegalArgumentException("Seat " + seat.getSeatNumber() + " is already booked");
            }
        }
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setSlotId(booking.getSlot().getSlotId());
        response.setMovieTitle(booking.getSlot().getMovie().getTitle());
        response.setTheaterName(booking.getSlot().getTheaterName());
        response.setScreenType(booking.getSlot().getScreenType());
        response.setShowDateTime(booking.getSlot().getShowDate().atTime(booking.getSlot().getStartTime()));
        response.setUserEmail(booking.getUserEmail());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingTime(booking.getBookingTime());
        response.setStatus(booking.getStatus());
        response.setPaymentId(booking.getPaymentId());
        response.setPaymentStatus(booking.getPaymentStatus());

        // Handle seat numbers with null check and logging
        List<String> seatNumbers = booking.getSeatNumbers();
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            log.warn("Booking ID {} has null or empty seat numbers", booking.getBookingId());
            response.setSeatNumbers(null);
        } else {
            response.setSeatNumbers(seatNumbers);
            log.debug("Booking ID {} seat numbers: {}", booking.getBookingId(), seatNumbers);
        }

        // Set verification fields
        response.setQrCode(booking.getQrCode());
        response.setVerified(Boolean.TRUE.equals(booking.getIsVerified()));
        if (booking.getVerificationTime() != null) {
            response.setVerificationTime(booking.getVerificationTime().toString());
        }
        response.setVerifiedBy(booking.getVerifiedBy());

        return response;
    }

    // Admin methods
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllWithDetails();
        return bookings.stream()
                .map(booking -> convertToBookingResponse(booking))
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsBySlot(Long slotId) {
        MovieSlot slot = movieSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));

        List<Booking> bookings = bookingRepository.findBySlot(slot);
        return bookings.stream()
                .map(booking -> convertToBookingResponse(booking))
                .collect(Collectors.toList());
    }

    // Method to fix existing bookings with null seat numbers
    @Transactional
    public void fixNullSeatNumbers() {
        log.info("Starting to fix bookings with null seat numbers...");

        List<Booking> allBookings = bookingRepository.findAll();
        int fixedCount = 0;

        for (Booking booking : allBookings) {
            if (booking.getSeatNumbers() == null || booking.getSeatNumbers().isEmpty()) {
                log.warn("Found booking with null/empty seat numbers: ID {}", booking.getBookingId());

                // Set a default value or try to derive from other data
                // For demonstration, setting a placeholder - in real scenario you might
                // want to derive this from historical data or mark as unknown
                List<String> defaultSeats = List.of("Unknown");
                booking.setSeatNumbers(defaultSeats);
                bookingRepository.save(booking);
                fixedCount++;

                log.info("Fixed booking ID {} with default seat numbers", booking.getBookingId());
            }
        }

        log.info("Fixed {} bookings with null seat numbers", fixedCount);
    }

    // Admin methods for ticket verification
    public List<TicketVerificationDto> getTodayTicketsForCinema(String cinemaName) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Booking> todayBookings = bookingRepository.findByBookingTimeBetween(startOfDay, endOfDay);

        return todayBookings.stream()
                .filter(booking -> booking.getSlot().getTheaterName().equalsIgnoreCase(cinemaName))
                .filter(booking -> Booking.BookingStatus.CONFIRMED.equals(booking.getStatus()))
                .map(booking -> {
                    try {
                        return ticketVerificationService.getTicketVerificationDetails(booking.getBookingId());
                    } catch (Exception e) {
                        log.warn("Could not get verification details for booking {}: {}", booking.getBookingId(),
                                e.getMessage());
                        return null;
                    }
                })
                .filter(ticket -> ticket != null)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getVerificationSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Get total confirmed bookings
        long totalTickets = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);

        // Get verified tickets count
        long verifiedTickets = bookingRepository.findByIsVerified(true).size();

        // Calculate pending verification
        long pendingVerification = totalTickets - verifiedTickets;

        // Calculate verification rate
        double verificationRate = totalTickets > 0 ? (double) verifiedTickets / totalTickets * 100 : 0.0;

        // Get today's verifications
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long todayVerifications = bookingRepository.findAllWithDetails().stream()
                .filter(booking -> Boolean.TRUE.equals(booking.getIsVerified()))
                .filter(booking -> booking.getVerificationTime() != null)
                .filter(booking -> booking.getVerificationTime().isAfter(startOfDay) &&
                        booking.getVerificationTime().isBefore(endOfDay))
                .count();

        // Get recent verifications (last 10)
        List<Map<String, Object>> recentVerifications = bookingRepository.findAllWithDetails().stream()
                .filter(booking -> Boolean.TRUE.equals(booking.getIsVerified()))
                .filter(booking -> booking.getVerificationTime() != null)
                .sorted((b1, b2) -> b2.getVerificationTime().compareTo(b1.getVerificationTime()))
                .limit(10)
                .map(booking -> {
                    Map<String, Object> verification = new HashMap<>();
                    verification.put("bookingId", booking.getBookingId());
                    verification.put("movieTitle", booking.getSlot().getMovie().getTitle());
                    verification.put("userEmail", booking.getUserEmail());
                    verification.put("verificationTime", booking.getVerificationTime().toString());
                    verification.put("verifiedBy", booking.getVerifiedBy());
                    return verification;
                })
                .collect(Collectors.toList());

        summary.put("totalTickets", totalTickets);
        summary.put("verifiedTickets", verifiedTickets);
        summary.put("pendingVerification", pendingVerification);
        summary.put("verificationRate", Math.round(verificationRate * 100.0) / 100.0);
        summary.put("todayVerifications", todayVerifications);
        summary.put("recentVerifications", recentVerifications);

        return summary;
    }
}
