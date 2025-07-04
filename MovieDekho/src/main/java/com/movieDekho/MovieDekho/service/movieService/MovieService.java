package com.movieDekho.MovieDekho.service.movieService;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MovieService {

    private MovieRepository movieRepository;

    public List<AvailableMovie> getRecentMovies() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return movieRepository.findByReleaseDateAfter(oneYearAgo);
    }

    public Optional<AvailableMovie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }
}
