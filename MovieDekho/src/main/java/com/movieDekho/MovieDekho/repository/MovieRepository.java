package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<AvailableMovie, Long> {
    List<AvailableMovie> findByReleaseDateAfter(LocalDate releaseDateAfter);

    @NonNull
    @Override
    Optional<AvailableMovie> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"slots"})
    Optional<AvailableMovie> findMovieWithSlotsById(Long id);
}
