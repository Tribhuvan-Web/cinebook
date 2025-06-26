package com.movieDekho.MovieDekho.service;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {

    private MovieRepository movieRepository;

    public  void saveMovie(AvailableMovie availableMovie) {
        movieRepository.save(availableMovie);
    }
}
