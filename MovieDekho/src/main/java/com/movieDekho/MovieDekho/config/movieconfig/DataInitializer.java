package com.movieDekho.MovieDekho.config.movieconfig;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(MovieRepository repository) {
        return args -> {
            LocalDate startOfRange = LocalDate.now();
            LocalDate endOfRange = startOfRange.plusDays(5);


            repository.saveAll(Arrays.asList(
                    new AvailableMovie(
                            1L,
                            "F1: The Movie",
                            LocalDate.of(2025, 7, 3),
                            "2h 55m",
                            "Racing thriller directed by Reyansh Singh",
                            "U/A 16+",
                            "http://localhost:8080/movies/recent",
                            startOfRange, endOfRange
                    ),

                    // Most recent movies (2024-2025 releases)
                    new AvailableMovie(
                            2L,
                            "Deadpool & Wolverine",
                            LocalDate.of(2024, 7, 26),
                            "2h 7m",
                            "The merc with a mouth teams up with Wolverine",
                            "U/A 16+",
                            "https://example.com/deadpool-wolverine.jpg",
                            startOfRange, endOfRange
                    ),

                    new AvailableMovie(
                            3L,
                            "Joker: Folie à Deux",
                            LocalDate.of(2024, 10, 4),
                            "2h 35m",
                            "Musical sequel to the 2019 Joker film",
                            "A+",
                            "https://example.com/joker2.jpg",
                            startOfRange, endOfRange
                    ),

                    new AvailableMovie(
                            4L,
                            "Avatar 3",
                            LocalDate.of(2025, 12, 19),
                            "3h 10m",
                            "Next chapter in the Pandora saga",
                            "U/A 16+",
                            "https://example.com/avatar3.jpg",
                            startOfRange, endOfRange
                    ),

                    new AvailableMovie(
                            5L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.of(2025, 5, 23),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "U/A",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    ),
                    new AvailableMovie(
                            6L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.of(2025, 5, 23),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "U/A",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    ),
                    new AvailableMovie(
                            7L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.of(2025, 5, 23),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "A+",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    ),
                    new AvailableMovie(
                            8L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.of(2025, 5, 23),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "U/A",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    ),
                    new AvailableMovie(
                            9L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.of(2025, 5, 23),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "U/A 7+",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    ),
                    new AvailableMovie(
                            10L,
                            "Mission: Impossible - Dead Reckoning Part Two",
                            LocalDate.now(),
                            "2h 45m",
                            "Ethan Hunt's final mission",
                            "A+",
                            "https://example.com/mi8.jpg",
                            startOfRange, endOfRange
                    )
            ));

            System.out.println("Dummy movies inserted into database");
        };
    }
}
