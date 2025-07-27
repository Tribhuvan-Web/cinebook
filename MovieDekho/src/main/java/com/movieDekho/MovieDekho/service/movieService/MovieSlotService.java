package com.movieDekho.MovieDekho.service.movieService;

import com.movieDekho.MovieDekho.dtos.movie.MovieSlotDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotResponseDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieSlotUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.repository.MovieSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieSlotService {

    private final MovieSlotRepository movieSlotRepository;
    private final MovieRepository movieRepository;

    public MovieSlotResponseDTO createMovieSlot(MovieSlotDTO request) {
        // Validate movie exists
        AvailableMovie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + request.getMovieId()));

        // Validate show date is within movie's date range
        LocalDate showDate = request.getShowDate();
        if (showDate != null && (showDate.isBefore(movie.getStartDate()) || showDate.isAfter(movie.getEndDate()))) {
            throw new IllegalArgumentException("Show date must be between " +
                    movie.getStartDate() + " and " + movie.getEndDate());
        }

        // Create and save movie slot
        MovieSlot slot = new MovieSlot();
        slot.setMovie(movie);
        slot.setShowDate(request.getShowDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setTheaterName(request.getTheaterName());
        slot.setScreenType(request.getScreenType());
        slot.setTotalSeats(request.getTotalSeats());
        slot.setAvailableSeats(request.getAvailableSeats());

        MovieSlot savedSlot = movieSlotRepository.save(slot);
        return MovieSlotResponseDTO.fromMovieSlot(savedSlot);
    }

    /**
     * Get all movie slots
     */
    public List<MovieSlotResponseDTO> getAllSlots() {
        List<MovieSlot> slots = movieSlotRepository.findAll();
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Update a movie slot
     */
    public MovieSlotResponseDTO updateMovieSlot(Long slotId, MovieSlotUpdateRequest request) {
        MovieSlot slot = movieSlotRepository.findBySlotId(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));

        // Validate show date against movie's date range if provided
        if (request.getShowDate() != null) {
            LocalDate showDate = request.getShowDate();
            AvailableMovie movie = slot.getMovie();
            if (showDate.isBefore(movie.getStartDate()) || showDate.isAfter(movie.getEndDate())) {
                throw new IllegalArgumentException("Show date must be between " +
                        movie.getStartDate() + " and " + movie.getEndDate());
            }
            slot.setShowDate(showDate);
        }

        // Update fields if provided
        if (request.getStartTime() != null) slot.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) slot.setEndTime(request.getEndTime());
        if (request.getTheaterName() != null) slot.setTheaterName(request.getTheaterName());
        if (request.getScreenType() != null) slot.setScreenType(request.getScreenType());
        if (request.getTotalSeats() > 0) slot.setTotalSeats(request.getTotalSeats());
        if (request.getAvailableSeats() >= 0) slot.setAvailableSeats(request.getAvailableSeats());

        MovieSlot updatedSlot = movieSlotRepository.save(slot);
        return MovieSlotResponseDTO.fromMovieSlot(updatedSlot);
    }

    /**
     * Delete a movie slot
     */
    public void deleteMovieSlot(Long slotId) {
        if (!movieSlotRepository.findBySlotId(slotId).isPresent()) {
            throw new ResourceNotFoundException("Movie slot not found with ID: " + slotId);
        }
        
        movieSlotRepository.deleteById(slotId);
    }

    // ============ USER SERVICES ============

    /**
     * Get all slots for a specific movie
     */
    public List<MovieSlotResponseDTO> getSlotsByMovie(Long movieId) {
        List<MovieSlot> slots = movieSlotRepository.findByMovieId(movieId);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Get slots by movie and date
     */
    public List<MovieSlotResponseDTO> getSlotsByMovieAndDate(Long movieId, LocalDate date) {
        List<MovieSlot> slots = movieSlotRepository.findByMovieIdAndShowDate(movieId, date);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Get slot by ID
     */
    public MovieSlotResponseDTO getSlotById(Long slotId) {
        MovieSlot slot = movieSlotRepository.findBySlotId(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));
        
        return MovieSlotResponseDTO.fromMovieSlot(slot);
    }

    /**
     * Get available slots (slots with available seats > 0)
     */
    public List<MovieSlotResponseDTO> getAvailableSlots() {
        List<MovieSlot> slots = movieSlotRepository.findByAvailableSeatsGreaterThan(0);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Search slots by theater name
     */
    public List<MovieSlotResponseDTO> getSlotsByTheater(String theaterName) {
        List<MovieSlot> slots = movieSlotRepository.findByTheaterNameContainingIgnoreCase(theaterName);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Get slots by date range
     */
    public List<MovieSlotResponseDTO> getSlotsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<MovieSlot> slots = movieSlotRepository.findByShowDateBetween(startDate, endDate);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    /**
     * Search slots by movie, date and theater
     */
    public List<MovieSlotResponseDTO> searchSlots(Long movieId, LocalDate date, String theaterName) {
        List<MovieSlot> slots = movieSlotRepository.findByMovieIdAndShowDateAndTheaterName(movieId, date, theaterName);
        return slots.stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList());
    }

    // ============ UTILITY METHODS ============

    /**
     * Check if a slot exists
     */
    public boolean slotExists(Long slotId) {
        return movieSlotRepository.findBySlotId(slotId).isPresent();
    }

    /**
     * Update available seats count (useful for booking operations)
     */
    public void updateAvailableSeats(Long slotId, int newAvailableSeats) {
        MovieSlot slot = movieSlotRepository.findBySlotId(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));
        
        if (newAvailableSeats < 0 || newAvailableSeats > slot.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats must be between 0 and " + slot.getTotalSeats());
        }
        
        slot.setAvailableSeats(newAvailableSeats);
        movieSlotRepository.save(slot);
    }
}
