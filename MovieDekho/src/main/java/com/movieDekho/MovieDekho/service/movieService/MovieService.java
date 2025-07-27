package com.movieDekho.MovieDekho.service.movieService;

import com.movieDekho.MovieDekho.dtos.movie.MovieCreateRequest;
import com.movieDekho.MovieDekho.dtos.movie.MovieUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieService {

    private MovieRepository movieRepository;

    public List<MovieResponseDTO> getRecentMovies() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return movieRepository.findByReleaseDateAfter(oneYearAgo)
                .stream()
                .map(MovieResponseDTO::fromMovie)
                .collect(Collectors.toList());
    }

    public MovieResponseDTO getMovieById(Long id) {
        AvailableMovie movie = movieRepository.findMovieWithSlotsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return MovieResponseDTO.fromMovie(movie);
    }

    public List<MovieResponseDTO> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(MovieResponseDTO::fromMovie)
                .collect(Collectors.toList());
    }

    public List<MovieResponseDTO> searchMovies(String query) {
        return movieRepository.findByTitleContainingIgnoreCase(query)
                .stream()
                .map(MovieResponseDTO::fromMovie)
                .collect(Collectors.toList());
    }

    public List<MovieResponseDTO> sortMoviesByTitle(List<MovieResponseDTO> movies) {
        return movies.stream()
                .sorted((m1, m2) -> m1.getTitle().compareToIgnoreCase(m2.getTitle()))
                .collect(Collectors.toList());
    }

    public List<MovieResponseDTO> sortMoviesByReleaseDate(List<MovieResponseDTO> movies) {
        return movies.stream()
                .sorted((m1, m2) -> m2.getReleaseDate().compareTo(m1.getReleaseDate())) // Latest first
                .collect(Collectors.toList());
    }

    public List<MovieResponseDTO> filterMovies(String filterType, String title, String sortBy) {
        List<MovieResponseDTO> movies = getAllMovies();
        
        // Apply filter type
        if ("recent".equalsIgnoreCase(filterType)) {
            movies = getRecentMovies();
        }
        
        // Apply title filter
        if (title != null && !"null".equalsIgnoreCase(title) && !title.trim().isEmpty()) {
            movies = movies.stream()
                    .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }
        
        // Apply sorting
        if ("title".equalsIgnoreCase(sortBy)) {
            movies = sortMoviesByTitle(movies);
        } else if ("release_date".equalsIgnoreCase(sortBy)) {
            movies = sortMoviesByReleaseDate(movies);
        }
        
        return movies;
    }

    public MovieResponseDTO createMovie(MovieCreateRequest request) {
        AvailableMovie movie = new AvailableMovie();
        movie.setTitle(request.getTitle());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setDuration(request.getDuration());
        movie.setDescription(request.getDescription());
        movie.setCertification(request.getCertification());
        movie.setThumbnail(request.getThumbnail());
        movie.setTrailer(request.getTrailer());
        movie.setGenre(request.getGenre());
        movie.setStartDate(request.getStartDate());
        movie.setEndDate(request.getEndDate());

        AvailableMovie savedMovie = movieRepository.save(movie);
        return MovieResponseDTO.fromMovie(savedMovie);
    }

    public MovieResponseDTO updateMovie(Long id, MovieUpdateRequest request) {
        Optional<AvailableMovie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isEmpty()) {
            throw new ResourceNotFoundException("Movie not found with ID: " + id);
        }

        AvailableMovie movie = movieOptional.get();

        if (request.getTitle() != null)
            movie.setTitle(request.getTitle());
        if (request.getReleaseDate() != null)
            movie.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null)
            movie.setDuration(request.getDuration());
        if (request.getDescription() != null)
            movie.setDescription(request.getDescription());
        if (request.getCertification() != null)
            movie.setCertification(request.getCertification());
        if (request.getThumbnail() != null)
            movie.setThumbnail(request.getThumbnail());
        if (request.getTrailer() != null)
            movie.setTrailer(request.getTrailer());
        if (request.getGenre() != null)
            movie.setGenre(request.getGenre());
        if (request.getStartDate() != null)
            movie.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)
            movie.setEndDate(request.getEndDate());

        AvailableMovie updatedMovie = movieRepository.save(movie);
        return MovieResponseDTO.fromMovie(updatedMovie);
    }

    public void deleteMovie(Long id) {
        Optional<AvailableMovie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isEmpty()) {
            throw new ResourceNotFoundException("Movie not found with ID: " + id);
        }
        movieRepository.deleteById(id);
    }
}
