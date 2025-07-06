package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.seat.SeatRequest;
import com.movieDekho.MovieDekho.dtos.seat.SeatResponse;
import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.models.Seat;
import com.movieDekho.MovieDekho.repository.MovieSlotRepository;
import com.movieDekho.MovieDekho.repository.SeatRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movie/slots")
@AllArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;
    private final MovieSlotRepository movieSlotRepository;

    @PostMapping("/{slotId}/seats")
    public ResponseEntity<?> createSeats(
            @PathVariable Long slotId,
            @RequestBody List<SeatRequest> seatRequests) {

        try {
            Optional<MovieSlot> slotOptional = movieSlotRepository.findById(slotId);
            if (slotOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Movie slot not found with ID: " + slotId);
            }

            MovieSlot slot = slotOptional.get();

            List<Seat> seats = seatRequests.stream()
                    .map(request -> {
                        Seat seat = new Seat();
                        seat.setSlot(slot);
                        seat.setSeatNumber(request.getSeatNumber());
                        seat.setPrice(request.getPrice());
                        seat.setBooked(false); // Default to available
                        return seat;
                    })
                    .collect(Collectors.toList());

            List<Seat> savedSeats = seatRepository.saveAll(seats);

            slot.setTotalSeats(slot.getTotalSeats() + seats.size());
            slot.setAvailableSeats(slot.getAvailableSeats() + seats.size());
            movieSlotRepository.save(slot);

            List<SeatResponse> response = savedSeats.stream()
                    .map(seat -> {
                        SeatResponse dto = new SeatResponse();
                        dto.setSeatId(seat.getSeatId());
                        dto.setSeatNumber(seat.getSeatNumber());
                        dto.setBooked(seat.isBooked());
                        dto.setPrice(seat.getPrice());
                        dto.setSlotId(seat.getSlot().getSlotId());
                        return dto;
                    })
                    .toList();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating seats: " + e.getMessage());
        }
    }
}