package com.movieDekho.MovieDekho.service.userService;

import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.util.MovieMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FavoritesService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public String addToFavorites(String userIdentifier, Long movieId) {
        Optional<User> userOpt = userRepository.findByEmailOrPhoneWithFavorites(userIdentifier);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Optional<AvailableMovie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) {
            throw new ResourceNotFoundException("Movie not found with ID: " + movieId);
        }

        User user = userOpt.get();
        AvailableMovie movie = movieOpt.get();

        if (user.getFavoriteMovies().contains(movie)) {
            return "Movie is already in your favorites";
        }

        user.getFavoriteMovies().add(movie);
        userRepository.save(user);

        return "Movie added to favorites successfully";
    }

    @Transactional
    public String removeFromFavorites(String userIdentifier, Long movieId) {
        Optional<User> userOpt = userRepository.findByEmailOrPhoneWithFavorites(userIdentifier);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOpt.get();
        boolean removed = user.getFavoriteMovies().removeIf(movie -> movie.getId().equals(movieId));
        if (!removed) {
            return "Movie was not in your favorites";
        }

        userRepository.save(user);
        return "Movie removed from favorites successfully";
    }

    @Transactional
    public void removeAllFavorites(String userIdentifier) {
        Optional<User> userOpt = userRepository.findByEmailOrPhoneWithFavorites(userIdentifier);
        User user = userOpt.get();
        user.getFavoriteMovies().clear();
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getFavoriteMovies(String userIdentifier) {
        Optional<User> userOpt = userRepository.findByEmailOrPhoneWithFavorites(userIdentifier);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOpt.get();
        return user.getFavoriteMovies().stream()
                .map(MovieMapper::toMovieResponseDTO)
                .collect(Collectors.toList());
    }

}
