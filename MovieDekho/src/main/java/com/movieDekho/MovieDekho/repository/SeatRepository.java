package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
}
