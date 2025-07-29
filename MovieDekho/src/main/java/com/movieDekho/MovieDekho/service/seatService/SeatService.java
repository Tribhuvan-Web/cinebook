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

    public List<SeatResponse> createSeats(Long slotId, List<SeatRequest> seatRequests) {
        MovieSlot slot = getMovieSlotById(slotId);

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

        List<Seat> seats = seatRequests.stream()
                .map(request -> createSeatEntity(slot, request))
                .collect(Collectors.toList());

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        updateSlotTotals(slot, seats.size(), seats.size());

        return savedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getAllSeatsForSlot(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = seatRepository.findBySlot(slot);
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = getSeatById(seatId);

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

    public void deleteSeat(Long seatId) {
        Seat seat = getSeatById(seatId);
        MovieSlot slot = seat.getSlot();

        seatRepository.delete(seat);

        int totalSeatsChange = -1;
        int availableSeatsChange = seat.isBooked() ? 0 : -1;
        updateSlotTotals(slot, totalSeatsChange, availableSeatsChange);
    }

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

    public List<SeatResponse> bulkCreateSeats(Long slotId, String rowStart, String rowEnd,
                                              int seatsPerRow, double price) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = generateSeats(slot, rowStart, rowEnd, seatsPerRow, price);

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        updateSlotTotals(slot, seats.size(), seats.size());

        return savedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getAvailableSeats(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> availableSeats = seatRepository.findBySlotAndIsBooked(slot, false);
        return availableSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getBookedSeats(Long slotId) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> bookedSeats = seatRepository.findBySlotAndIsBooked(slot, true);
        return bookedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getAllSeatsWithStatus(Long slotId) {
        return getAllSeatsForSlot(slotId);
    }

    public SeatResponse getSeatResponseById(Long seatId) {
        Seat seat = getSeatById(seatId);
        return convertToSeatResponse(seat);
    }

    public SeatAvailabilityResponse checkSeatAvailability(Long seatId) {
        Seat seat = getSeatById(seatId);
        return new SeatAvailabilityResponse(
                seat.getSeatId(),
                seat.getSeatNumber(),
                !seat.isBooked(),
                seat.getPrice()
        );
    }

    public List<SeatResponse> getSeatsByPriceRange(Long slotId, double minPrice, double maxPrice) {
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = seatRepository.findBySlotAndPriceBetween(slot, minPrice, maxPrice);
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    private MovieSlot getMovieSlotById(Long slotId) {
        return movieSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));
    }

    private Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));
    }

    private Seat createSeatEntity(MovieSlot slot, SeatRequest request) {
        Seat seat = new Seat();
        seat.setSlot(slot);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setPrice(request.getPrice());
        seat.setBooked(false); // Default to available
        return seat;
    }

    private void validateUniquesSeatNumber(Seat currentSeat, String newSeatNumber) {
        Optional<Seat> existingSeat = seatRepository.findBySlotAndSeatNumber(
                currentSeat.getSlot(), newSeatNumber);
        if (existingSeat.isPresent() && !existingSeat.get().getSeatId().equals(currentSeat.getSeatId())) {
            throw new IllegalArgumentException("Seat number already exists: " + newSeatNumber);
        }
    }

    private void updateSeatBookingStatus(Seat seat, boolean newBookingStatus) {
        boolean previousBookingStatus = seat.isBooked();
        if (previousBookingStatus != newBookingStatus) {
            seat.setBooked(newBookingStatus);
            updateAvailableSeatsCount(seat.getSlot(), previousBookingStatus, newBookingStatus);
        }
    }

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

    private void updateSlotTotals(MovieSlot slot, int totalSeatsChange, int availableSeatsChange) {
        slot.setTotalSeats(slot.getTotalSeats() + totalSeatsChange);
        slot.setAvailableSeats(slot.getAvailableSeats() + availableSeatsChange);
        movieSlotRepository.save(slot);
    }

    private SeatResponse convertToSeatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setSeatId(seat.getSeatId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setBooked(seat.isBooked());
        response.setPrice(seat.getPrice());
        response.setSlotId(seat.getSlot().getSlotId());
        return response;
    }

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

        public Long getSeatId() {
            return seatId;
        }

        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
