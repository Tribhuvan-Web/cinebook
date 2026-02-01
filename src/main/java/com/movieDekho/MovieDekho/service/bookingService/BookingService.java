package com.movieDekho.MovieDekho.service.bookingService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.movieDekho.MovieDekho.dtos.booking.BookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.BookingResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatAvailabilityResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatStatusResponse;
import com.movieDekho.MovieDekho.dtos.booking.PaymentBookingRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.exception.SeatAlreadySelectedException;
import com.movieDekho.MovieDekho.models.*;
import com.movieDekho.MovieDekho.repository.*;
import com.movieDekho.MovieDekho.service.temporarySeatLockService.TemporarySeatLockService;
import com.movieDekho.MovieDekho.service.ticketVerificationService.TicketVerificationService;
import com.movieDekho.MovieDekho.dtos.booking.TicketVerificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

// If using a custom PageEventHelper class, you might need this import:
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MovieSlotRepository movieSlotRepository;
    private final SeatRepository seatRepository;
    private final TemporarySeatLockRepository temporaryLockRepository;
    private final ObjectMapper objectMapper;
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
     * Get comprehensive seat status for a slot (booked, locked, available)
     * Similar to CineBook/BookMyShow seat status API
     * OPTIMIZED: Reduced from N+1 queries to just 3 queries total
     */
    public SeatStatusResponse getSeatStatusForSlot(Long slotId, String sessionId) {
        try {
            MovieSlot slot = movieSlotRepository.findById(slotId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Movie slot not found with ID: " + slotId));

            // Get all seats for this slot
            List<Seat> allSeats = seatRepository.findBySlot(slot);

            // OPTIMIZATION: Get all booked seat numbers for this slot in a single query
            List<String> bookedSeatNumbers = bookingRepository.findBookedSeatNumbersBySlot(slot);
            Set<String> bookedSeatSet = new HashSet<>(bookedSeatNumbers);

            // Get all active temporary locks for this slot
            List<TemporarySeatLock> activeLocks = temporaryLockRepository
                    .findActiveLocksBySlots(List.of(slot), LocalDateTime.now());

            // Create a map for quick lookup of locks by seat3
            Map<Long, TemporarySeatLock> lockMap = activeLocks.stream()
                    .collect(Collectors.toMap(
                            lock -> lock.getSeat().getSeatId(),
                            lock -> lock));
                            
            // Build seat info list
            List<SeatStatusResponse.SeatInfo> seatInfoList = allSeats.stream()
                    .map(seat -> {
                        SeatStatusResponse.SeatInfo seatInfo = new SeatStatusResponse.SeatInfo();
                        seatInfo.setSeatNumber(seat.getSeatNumber());
                        seatInfo.setPrice(seat.getPrice());

                        // Determine seat status - OPTIMIZED: Use Set lookup instead of individual
                        // queries
                        if (seat.isBooked() || bookedSeatSet.contains(seat.getSeatNumber())) {
                            seatInfo.setStatus(SeatStatusResponse.SeatStatus.BOOKED);
                        } else if (lockMap.containsKey(seat.getSeatId())) {
                            TemporarySeatLock lock = lockMap.get(seat.getSeatId());
                            if (sessionId != null && sessionId.equals(lock.getSessionId())) {
                                seatInfo.setStatus(SeatStatusResponse.SeatStatus.LOCKED_BY_YOU);
                            } else {
                                seatInfo.setStatus(SeatStatusResponse.SeatStatus.LOCKED);
                            }
                            seatInfo.setLockedBySession(lock.getSessionId());
                            seatInfo.setLockExpiresAt(lock.getExpiresAt().toString());
                        } else {
                            seatInfo.setStatus(SeatStatusResponse.SeatStatus.AVAILABLE);
                        }

                        return seatInfo;
                    })
                    .collect(Collectors.toList());

            // Calculate summary
            SeatStatusResponse.SeatSummary summary = new SeatStatusResponse.SeatSummary();
            summary.setTotalSeats(allSeats.size());
            summary.setBookedSeats((int) seatInfoList.stream()
                    .filter(seat -> seat.getStatus() == SeatStatusResponse.SeatStatus.BOOKED)
                    .count());
            summary.setLockedSeats((int) seatInfoList.stream()
                    .filter(seat -> seat.getStatus() == SeatStatusResponse.SeatStatus.LOCKED ||
                            seat.getStatus() == SeatStatusResponse.SeatStatus.LOCKED_BY_YOU)
                    .count());
            summary.setAvailableSeats(summary.getTotalSeats() - summary.getBookedSeats() - summary.getLockedSeats());

            // Build response
            SeatStatusResponse response = new SeatStatusResponse();
            response.setSlotId(slot.getSlotId());
            response.setMovieTitle(slot.getMovie().getTitle());
            response.setCinemaName(slot.getTheaterName());
            response.setScreenType(slot.getScreenType());
            response.setShowDate(slot.getShowDate().toString());
            response.setShowTime(slot.getStartTime().toString());
            response.setSeats(seatInfoList);
            response.setSummary(summary);

            log.info("Seat status retrieved for slot {} with {} seats in optimized manner", slotId, allSeats.size());
            return response;

        } catch (Exception e) {
            log.error("Failed to get seat status for slot: {}", slotId, e);
            throw new RuntimeException("Failed to get seat status: " + e.getMessage());
        }
    }



    /**
     * Create booking after Razorpay payment verification
     * This method should be called after payment has been verified through /api/payments/verify endpoint
     * @param razorpayRequest Contains slot, seat numbers, amounts, and Razorpay payment/order details
     * @param userEmail User email from JWT token
     * @return BookingResponse with confirmed booking details
     */
    @Transactional
    public BookingResponse createBookingAfterRazorpayPayment(
            com.movieDekho.MovieDekho.controller.BookingController.RazorpayBookingRequest razorpayRequest, 
            String userEmail) {

        log.info("Starting booking creation after Razorpay payment for user: {} - Payment: {}",
                userEmail, razorpayRequest.getPaymentId());

        // Validate user
        User user = userRepository.findByEmailOrPhone(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Validate movie slot
        MovieSlot slot = movieSlotRepository.findById(razorpayRequest.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie slot not found with ID: " + razorpayRequest.getSlotId()));

        // Lock seats for pessimistic locking
        List<Seat> seats = seatRepository.findBySlotAndSeatNumbersWithLock(slot, razorpayRequest.getSeatNumbers());

        if (seats.size() != razorpayRequest.getSeatNumbers().size()) {
            List<String> foundSeatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();
            List<String> missingSeatNumbers = razorpayRequest.getSeatNumbers().stream()
                    .filter(seatNumber -> !foundSeatNumbers.contains(seatNumber))
                    .toList();
            throw new ResourceNotFoundException("Seats not found: " + missingSeatNumbers);
        }

        // Check seat availability
        List<String> unavailableSeats = seats.stream()
                .filter(seat -> seat.isBooked()
                        || bookingRepository.isSeatBookedForSlot(slot, seat.getSeatNumber()))
                .map(Seat::getSeatNumber)
                .toList();

        if (!unavailableSeats.isEmpty()) {
            throw new IllegalArgumentException("Seats no longer available: " + unavailableSeats);
        }

        // Validate total amount
        double seatAmount = seats.stream().mapToDouble(Seat::getPrice).sum();
        double basePrice = (seatAmount * 7) / 100;
        double taxPrice = basePrice + ((basePrice * 18) / 100);
        double totalAmount = Math.round((taxPrice) * 100.0) / 100.0 + seatAmount;

        if (Math.abs(totalAmount - razorpayRequest.getTotalAmount()) > 0.01) {
            throw new IllegalArgumentException("Total amount mismatch. Expected: " + totalAmount
                    + ", Provided: " + razorpayRequest.getTotalAmount());
        }

        // Book seats
        for (Seat seat : seats) {
            seat.setBooked(true);
        }
        seatRepository.saveAll(seats);

        // Create booking with Razorpay payment details
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSlot(slot);
        booking.setUserEmail(userEmail);
        booking.setTicketFee(seatAmount);
        booking.setConvenienceFee(taxPrice);
        booking.setTotalAmount(totalAmount);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentMethod("RAZORPAY");
        booking.setSeatNumbers(razorpayRequest.getSeatNumbers());
        booking.setPaymentStatus("COMPLETED");
        booking.setPaymentId(razorpayRequest.getPaymentId());

        // Store Razorpay details in payment details JSON
        try {
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("gateway", "Razorpay");
            paymentDetails.put("paymentId", razorpayRequest.getPaymentId());
            paymentDetails.put("orderId", razorpayRequest.getOrderId());
            paymentDetails.put("paymentMethod", "RAZORPAY");
            
            String paymentDetailsJson = objectMapper.writeValueAsString(paymentDetails);
            booking.setPaymentDetails(paymentDetailsJson);
        } catch (Exception e) {
            log.warn("Failed to serialize Razorpay payment details: ", e);
        }

        updateSlotAvailableSeats(slot, razorpayRequest.getSeatNumbers().size());
        booking = bookingRepository.save(booking);

        ticketVerificationService.generateVerificationDataForBooking(booking.getBookingId());

        log.info("Booking created successfully after Razorpay payment: {} - Payment: {} - Seats: {}",
                booking.getBookingId(), razorpayRequest.getPaymentId(), razorpayRequest.getSeatNumbers());

        return convertToBookingResponse(booking);
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

        // TODO: Process refund through Razorpay if payment was successful
        // Use RazorpayPaymentService.refundPayment() with booking.getPaymentId()
        if ("COMPLETED".equals(booking.getPaymentStatus()) && booking.getPaymentId() != null) {
            log.warn("Manual refund required for payment ID: {} - Booking ID: {}", 
                    booking.getPaymentId(), bookingId);
            // Admin should process refund through Razorpay dashboard or use PaymentController refund endpoint
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
        response.setMovieThumbnail(booking.getSlot().getMovie().getThumbnail());
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

    /**
     * SIMPLIFIED: Generate a clean, compact ticket with only essential information
     */
    public byte[] generateTicketFromBooking(BookingResponse booking) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5, 20, 20, 20, 20); // Smaller A5 size for compactness

        PdfWriter.getInstance(document, baos);
        document.open();

        // Simple color scheme
        BaseColor primaryColor = new BaseColor(33, 150, 243); // Blue
        BaseColor textColor = new BaseColor(51, 51, 51); // Dark Gray

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, primaryColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, textColor);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, textColor);
        Font qrFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, textColor);

        // Main container
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        // Header with title
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(PdfPCell.NO_BORDER);
        headerCell.setPadding(15);
        headerCell.setBackgroundColor(new BaseColor(245, 245, 245));

        Paragraph title = new Paragraph("🎬 MOVIE TICKET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        Paragraph bookingId = new Paragraph("Booking #" + booking.getBookingId(), normalFont);
        bookingId.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(bookingId);

        mainTable.addCell(headerCell);

        // Content table with two columns
        PdfPTable contentTable = new PdfPTable(2);
        contentTable.setWidthPercentage(100);
        contentTable.setWidths(new float[] { 2f, 1f });

        // Left column - Movie details
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(PdfPCell.NO_BORDER);
        leftCell.setPadding(15);

        // Movie info
        addSimpleField(leftCell, "🎭 Movie", booking.getMovieTitle(), headerFont, normalFont);
        addSimpleField(leftCell, "🏢 Theater", booking.getTheaterName(), headerFont, normalFont);
        addSimpleField(leftCell, "📺 Screen", booking.getScreenType(), headerFont, normalFont);

        // Show details
        if (booking.getShowDateTime() != null) {
            addSimpleField(leftCell, "📅 Date", booking.getShowDateTime().toLocalDate().toString(), headerFont,
                    normalFont);
            addSimpleField(leftCell, "🕐 Time", booking.getShowDateTime().toLocalTime().toString(), headerFont,
                    normalFont);
        }

        // Seat info
        String seats = booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()
                ? String.join(", ", booking.getSeatNumbers())
                : "N/A";
        addSimpleField(leftCell, "💺 Seats", seats, headerFont, normalFont);
        addSimpleField(leftCell, "💰 Amount", "₹" + booking.getTotalAmount(), headerFont, normalFont);

        contentTable.addCell(leftCell);

        // Right column - QR Code
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(PdfPCell.NO_BORDER);
        rightCell.setPadding(15);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        // QR Code generation
        try {
            String qrCodeData = booking.getQrCode();
            
            if (qrCodeData == null || qrCodeData.trim().isEmpty()) {
                log.warn("QR code data is null or empty for booking: {}", booking.getBookingId());
                qrCodeData = "BOOKING:" + booking.getBookingId();
            }
            
            // Validate QR code data length
            if (qrCodeData.length() > 2950) {
                log.warn("QR code data too long ({} chars), truncating for booking: {}", 
                    qrCodeData.length(), booking.getBookingId());
                qrCodeData = qrCodeData.substring(0, 2950);
            }
            
            log.debug("Generating QR code for booking {} with data length: {}", 
                booking.getBookingId(), qrCodeData.length());
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Use larger size for better readability and error correction
            var bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 150, 150);

            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrBaos);

            Image qrImage = Image.getInstance(qrBaos.toByteArray());
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.scaleToFit(120, 120);

            Paragraph qrTitle = new Paragraph("SCAN TO ENTER", qrFont);
            qrTitle.setAlignment(Element.ALIGN_CENTER);
            qrTitle.setSpacingAfter(10);
            rightCell.addElement(qrTitle);

            rightCell.addElement(qrImage);
            
            log.info("Successfully generated QR code for booking: {}", booking.getBookingId());

        } catch (Exception e) {
            log.error("Failed to generate QR code for booking {}: {}", 
                booking.getBookingId(), e.getMessage(), e);
            Paragraph qrError = new Paragraph("QR Code\nUnavailable", qrFont);
            qrError.setAlignment(Element.ALIGN_CENTER);
            rightCell.addElement(qrError);
        }

        contentTable.addCell(rightCell);

        PdfPCell contentCell = new PdfPCell(contentTable);
        contentCell.setBorder(PdfPCell.NO_BORDER);
        mainTable.addCell(contentCell);

        // Footer
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(PdfPCell.NO_BORDER);
        footerCell.setPadding(10);
        footerCell.setBackgroundColor(new BaseColor(245, 245, 245));

        Paragraph footer = new Paragraph("Thank you for choosing CineBook! Enjoy your movie! 🍿", qrFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(footer);

        mainTable.addCell(footerCell);

        document.add(mainTable);
        document.close();
        return baos.toByteArray();
    }

    /**
     * Helper method to add simple field rows
     */
    private void addSimpleField(PdfPCell cell, String label, String value, Font labelFont, Font valueFont) {
        Paragraph fieldPara = new Paragraph();
        fieldPara.add(new Chunk(label + ": ", labelFont));
        fieldPara.add(new Chunk(value != null ? value : "N/A", valueFont));
        fieldPara.setSpacingAfter(5);
        cell.addElement(fieldPara);
    }

}
