package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.MovieSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieSlotRepository extends JpaRepository<MovieSlot, Long> {

    Optional<MovieSlot> findBySlotId(Long slotId);

    List<MovieSlot> findByMovieId(Long movieId);

    // Find slots by movie and date
    List<MovieSlot> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);

    // Find slots by theater name
    List<MovieSlot> findByTheaterNameContainingIgnoreCase(String theaterName);

    // Find slots by date range
    List<MovieSlot> findByShowDateBetween(LocalDate startDate, LocalDate endDate);

    // Find available slots (with available seats > 0)
    List<MovieSlot> findByAvailableSeatsGreaterThan(int availableSeats);

    // Find slots by movie, date and theater
    @Query("SELECT ms FROM MovieSlot ms WHERE ms.movie.id = :movieId AND ms.showDate = :showDate AND ms.theaterName LIKE %:theaterName%")
    List<MovieSlot> findByMovieIdAndShowDateAndTheaterName(@Param("movieId") Long movieId,
            @Param("showDate") LocalDate showDate,
            @Param("theaterName") String theaterName);
}