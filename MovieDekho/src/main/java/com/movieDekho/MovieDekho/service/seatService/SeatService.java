package com.movieDekho.MovieDekho.service.seatService;

import com.movieDekho.MovieDekho.dtos.seat.SeatRequest;
import com.movieDekho.MovieDekho.dtos.seat.SeatResponse;
import com.movieDekho.MovieDekho.dtos.seat.SeatUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.models.Seat;
import com.movieDekho.MovieDekho.repository.MovieSlotRepository;
import com.movieDekho.MovieDekho.repository.SeatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final MovieSlotRepository movieSlotRepository;

    // ============ ADMIN SERVICES ============

    /**
     * Create seats for a movie slot
     */
    public List<SeatResponse> createSeats(Long slotId, List<SeatRequest> seatRequests) {
        MovieSlot slot = getMovieSlotById(slotId);

        // Check for duplicate seat numbers in the request
        List<String> requestSeatNumbers = seatRequests.stream()
                .map(SeatRequest::getSeatNumber)
                .toList();

        List<Seat> existingSeats = seatRepository.findBySlotAndSeatNumberIn(slot, requestSeatNumbers);
        if (!existingSeats.isEmpty()) {
            List<String> duplicates = existingSeats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();
            throw new IllegalArgumentException("Seat numbers already exist: " + duplicates);
        }

        // Create seat entities
        List<Seat> seats = seatRequests.stream()
                .map(request -> createSeatEntity(slot, request))
                .collect(Collectors.toList());

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        // Update slot totals
        updateSlotTotals(slot, seats.size(), seats.size());

        return savedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all seats for a slot
     */
    public List<SeatResponse> getAllSeatsForSlot(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = seatRepository.findBySlot(slot);
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update seat details
     */
    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = getSeatById(seatId);

        // Update fields if provided
        if (request.getSeatNumber() != null) {
            validateUniquesSeatNumber(seat, request.getSeatNumber());
            seat.setSeatNumber(request.getSeatNumber());
        }
        if (request.getPrice() > 0) {
            seat.setPrice(request.getPrice());
        }
        if (request.getBooked() != null) {
            updateSeatBookingStatus(seat, request.getBooked());
        }

        Seat updatedSeat = seatRepository.save(seat);
        return convertToSeatResponse(updatedSeat);
    }

    /**
     * Delete a seat
     */
    public void deleteSeat(Long seatId) {
        Seat seat = getSeatById(seatId);
        MovieSlot slot = seat.getSlot();

        seatRepository.delete(seat);

        // Update slot totals
        int totalSeatsChange = -1;
        int availableSeatsChange = seat.isBooked() ? 0 : -1;
        updateSlotTotals(slot, totalSeatsChange, availableSeatsChange);
    }

    /**
     * Update seat booking status
     */
    public SeatResponse updateSeatBookingStatus(Long seatId, boolean isBooked) {
        Seat seat = getSeatById(seatId);
        boolean previousBookingStatus = seat.isBooked();

        if (previousBookingStatus != isBooked) {
            seat.setBooked(isBooked);
            updateAvailableSeatsCount(seat.getSlot(), previousBookingStatus, isBooked);
            seatRepository.save(seat);
        }

        return convertToSeatResponse(seat);
    }

    /**
     * Bulk create seats with pattern
     */
    public List<SeatResponse> bulkCreateSeats(Long slotId, String rowStart, String rowEnd, 
                                            int seatsPerRow, double price) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = generateSeats(slot, rowStart, rowEnd, seatsPerRow, price);

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        // Update slot totals
        updateSlotTotals(slot, seats.size(), seats.size());

        return savedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    // ============ USER SERVICES ============

    /**
     * Get available seats for a slot
     */
    public List<SeatResponse> getAvailableSeats(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> availableSeats = seatRepository.findBySlotAndIsBooked(slot, false);
        return availableSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get booked seats for a slot
     */
    public List<SeatResponse> getBookedSeats(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> bookedSeats = seatRepository.findBySlotAndIsBooked(slot, true);
        return bookedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all seats with status for a slot
     */
    public List<SeatResponse> getAllSeatsWithStatus(Long slotId) {
        return getAllSeatsForSlot(slotId);
    }

    /**
     * Get seat by ID
     */
    public SeatResponse getSeatResponseById(Long seatId) {
        Seat seat = getSeatById(seatId);
        return convertToSeatResponse(seat);
    }

    /**
     * Check seat availability
     */
    public SeatAvailabilityResponse checkSeatAvailability(Long seatId) {
        Seat seat = getSeatById(seatId);
        return new SeatAvailabilityResponse(
                seat.getSeatId(),
                seat.getSeatNumber(),
                !seat.isBooked(),
                seat.getPrice()
        );
    }

    /**
     * Get seats by price range
     */
    public List<SeatResponse> getSeatsByPriceRange(Long slotId, double minPrice, double maxPrice) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = seatRepository.findBySlotAndPriceBetween(slot, minPrice, maxPrice);
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    // ============ UTILITY METHODS ============

    /**
     * Get movie slot by ID with validation
     */
    private MovieSlot getMovieSlotById(Long slotId) {
        return movieSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));
    }

    /**
     * Get seat by ID with validation
     */
    private Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));
    }

    /**
     * Create seat entity from request
     */
    private Seat createSeatEntity(MovieSlot slot, SeatRequest request) {
        Seat seat = new Seat();
        seat.setSlot(slot);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setPrice(request.getPrice());
        seat.setBooked(false); // Default to available
        return seat;
    }

    /**
     * Validate unique seat number within slot
     */
    private void validateUniquesSeatNumber(Seat currentSeat, String newSeatNumber) {
        Optional<Seat> existingSeat = seatRepository.findBySlotAndSeatNumber(
                currentSeat.getSlot(), newSeatNumber);
        if (existingSeat.isPresent() && !existingSeat.get().getSeatId().equals(currentSeat.getSeatId())) {
            throw new IllegalArgumentException("Seat number already exists: " + newSeatNumber);
        }
    }

    /**
     * Update seat booking status and handle slot available seats
     */
    private void updateSeatBookingStatus(Seat seat, boolean newBookingStatus) {
        boolean previousBookingStatus = seat.isBooked();
        if (previousBookingStatus != newBookingStatus) {
            seat.setBooked(newBookingStatus);
            updateAvailableSeatsCount(seat.getSlot(), previousBookingStatus, newBookingStatus);
        }
    }

    /**
     * Update available seats count in slot
     */
    private void updateAvailableSeatsCount(MovieSlot slot, boolean previousStatus, boolean newStatus) {
        if (previousStatus && !newStatus) {
            // Seat was booked, now available
            slot.setAvailableSeats(slot.getAvailableSeats() + 1);
        } else if (!previousStatus && newStatus) {
            // Seat was available, now booked
            slot.setAvailableSeats(slot.getAvailableSeats() - 1);
        }
        movieSlotRepository.save(slot);
    }

    /**
     * Update slot totals (total seats and available seats)
     */
    private void updateSlotTotals(MovieSlot slot, int totalSeatsChange, int availableSeatsChange) {
        slot.setTotalSeats(slot.getTotalSeats() + totalSeatsChange);
        slot.setAvailableSeats(slot.getAvailableSeats() + availableSeatsChange);
        movieSlotRepository.save(slot);
    }

    /**
     * Convert Seat entity to SeatResponse DTO
     */
    private SeatResponse convertToSeatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setSeatId(seat.getSeatId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setBooked(seat.isBooked());
        response.setPrice(seat.getPrice());
        response.setSlotId(seat.getSlot().getSlotId());
        return response;
    }

    /**
     * Generate seats with pattern (A1-A10, B1-B10, etc.)
     */
    private List<Seat> generateSeats(MovieSlot slot, String rowStart, String rowEnd, 
                                   int seatsPerRow, double price) {
        List<Seat> seats = new java.util.ArrayList<>();
        char startChar = rowStart.charAt(0);
        char endChar = rowEnd.charAt(0);

        for (char row = startChar; row <= endChar; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = new Seat();
                seat.setSlot(slot);
                seat.setSeatNumber(row + String.valueOf(seatNum));
                seat.setPrice(price);
                seat.setBooked(false);
                seats.add(seat);
            }
        }
        return seats;
    }

    // ============ INNER CLASSES ============

    public static class SeatAvailabilityResponse {
        private Long seatId;
        private String seatNumber;
        private boolean available;
        private double price;

        public SeatAvailabilityResponse(Long seatId, String seatNumber, boolean available, double price) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.available = available;
            this.price = price;
        }

        // Getters and setters
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
