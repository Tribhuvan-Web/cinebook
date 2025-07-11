package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.MovieSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovieSlotRepository extends JpaRepository<MovieSlot, Long> {

    List<MovieSlot> findByMovieIdAndStartTimeBetween(
            Long movieId,
            LocalDateTime startOfRange,
            LocalDateTime endOfRange
    );
}