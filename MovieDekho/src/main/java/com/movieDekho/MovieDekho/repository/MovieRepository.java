package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<AvailableMovie, Long> {
    List<AvailableMovie> findByReleaseDateAfter(LocalDate releaseDateAfter);
}
