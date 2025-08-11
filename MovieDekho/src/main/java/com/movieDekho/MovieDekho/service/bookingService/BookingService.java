package com.movieDekho.MovieDekho.service.bookingService;

import com.movieDekho.MovieDekho.dtos.booking.BookingRequest;
import com.movieDekho.MovieDekho.dtos.booking.BookingResponse;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionRequest;
import com.movieDekho.MovieDekho.dtos.booking.SeatSelectionResponse;
import com.movieDekho.MovieDekho.dtos.booking.PaymentBookingRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.*;
import com.movieDekho.MovieDekho.repository.*;
import com.movieDekho.MovieDekho.service.paymentService.MockPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Select seats and calculate total amount before payment
     */
    public SeatSelectionResponse selectSeats(SeatSelectionRequest request, String userEmail) {
        try {
            // Validate movie slot
            MovieSlot slot = movieSlotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + request.getSlotId()));

            // Validate seats by seat numbers
            List<Seat> seats = validateAndGetSeatsBySeatNumbers(request.getSeatNumbers(), slot);
            
            // Check if seats are available
            checkSeatAvailability(seats, slot);

            // Calculate total amount
            double totalAmount = seats.stream().mapToDouble(Seat::getPrice).sum();

            // Build response
            SeatSelectionResponse response = new SeatSelectionResponse();
            response.setSlotId(slot.getSlotId());
            response.setSeatNumbers(request.getSeatNumbers());
            response.setTotalAmount(totalAmount);
            response.setMovieTitle(slot.getMovie().getTitle());
            response.setMovieDescription(slot.getMovie().getDescription());
            response.setCinemaName(slot.getTheaterName());
            response.setScreenName(slot.getScreenType());
            response.setShowTime(slot.getStartTime().toString());
            response.setShowDate(slot.getShowDate().toString());

            // Add seat details
            List<SeatSelectionResponse.SeatDetails> seatDetailsList = seats.stream()
                    .map(seat -> {
                        SeatSelectionResponse.SeatDetails seatDetails = new SeatSelectionResponse.SeatDetails();
                        seatDetails.setSeatNumber(seat.getSeatNumber());
                        seatDetails.setSeatType("STANDARD"); // Default seat type since not in model
                        seatDetails.setPrice(seat.getPrice());
                        return seatDetails;
                    })
                    .collect(Collectors.toList());
            response.setSeatDetails(seatDetailsList);

            log.info("Seats selected successfully for user: {} - Seats: {} - Total: {}", 
                    userEmail, request.getSeatNumbers(), totalAmount);
            
            // Store seat selection in cache for later booking
            seatSelectionCacheService.storeSeatSelection(userEmail, response);
            
            return response;

        } catch (Exception e) {
            log.error("Seat selection failed: ", e);
            throw new RuntimeException("Seat selection failed: " + e.getMessage());
        }
    }

    /**
     * Create booking using cached seat selection data
     */
    @Transactional
    public BookingResponse createBookingFromSeatSelection(PaymentBookingRequest paymentRequest, String userEmail) {
        try {
            // Get cached seat selection
            SeatSelectionResponse seatSelection = seatSelectionCacheService.getSeatSelection(userEmail, paymentRequest.getSlotId());
            if (seatSelection == null) {
                throw new IllegalArgumentException("No seat selection found. Please select seats first using /select-seats endpoint.");
            }

            // Validate user
            User user = userRepository.findByEmailOrPhone(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

            // Validate movie slot
            MovieSlot slot = movieSlotRepository.findById(paymentRequest.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + paymentRequest.getSlotId()));

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
                    paymentRequest.getCvv()
            );

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
                    .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + request.getSlotId()));

            // Validate seats by seat numbers
            List<Seat> seats = validateAndGetSeatsBySeatNumbers(request.getSeatNumbers(), slot);
            
            // Check if seats are available
            checkSeatAvailability(seats, slot);

            // Calculate total amount
            double calculatedTotal = seats.stream().mapToDouble(Seat::getPrice).sum();
            if (Math.abs(calculatedTotal - request.getTotalAmount()) > 0.01) {
                throw new IllegalArgumentException("Total amount mismatch. Expected: " + calculatedTotal + ", Provided: " + request.getTotalAmount());
            }

            // Process payment
            MockPaymentService.PaymentResponse paymentResponse = mockPaymentService.processPayment(
                    calculatedTotal,
                    request.getPaymentMethod(),
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv()
            );

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
                booking.getSeatNumbers()
        );
        markSeatsAsBooked(seats);
    }

    private void updateSlotAvailableSeats(MovieSlot slot, int bookedSeatsCount) {
        slot.setAvailableSeats(slot.getAvailableSeats() - bookedSeatsCount);
        movieSlotRepository.save(slot);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        List<Booking> bookings = bookingRepository.findByUserEmail(userEmail);
        return bookings.stream()
                .map(booking -> convertToBookingResponse(booking))
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
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
                        booking.getTotalAmount()
                );
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
                booking.getSeatNumbers()
        );
        
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
            
            // Additional check in booking table for confirmed bookings
            boolean isAlreadyBooked = bookingRepository.isSeatBookedForSlot(slot, seat.getSeatNumber());
            if (isAlreadyBooked) {
                throw new IllegalArgumentException("Seat " + seat.getSeatNumber() + " is already booked");
            }
        }
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setSlotId(booking.getSlot().getSlotId());
        response.setMovieTitle(booking.getSlot().getMovie().getTitle());
        response.setTheaterName(booking.getSlot().getTheaterName());
        response.setScreenType(booking.getSlot().getScreenType());
        response.setShowDateTime(booking.getSlot().getShowDate().atTime(booking.getSlot().getStartTime()));
        response.setSeatNumbers(booking.getSeatNumbers());
        response.setUserEmail(booking.getUserEmail());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingTime(booking.getBookingTime());
        response.setStatus(booking.getStatus());
        response.setPaymentId(booking.getPaymentId());
        response.setPaymentStatus(booking.getPaymentStatus());
        
        return response;
    }

    // Admin methods
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
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
}
