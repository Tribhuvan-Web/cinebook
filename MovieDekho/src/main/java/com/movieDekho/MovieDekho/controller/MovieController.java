package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.movie.MovieCreateRequest;
import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.dtos.movie.MovieUpdateRequest;
import com.movieDekho.MovieDekho.dtos.user.UserResponseDTO;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.repository.BookingRepository;
import com.movieDekho.MovieDekho.service.movieService.MovieService;
import com.movieDekho.MovieDekho.util.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/movies")
@RestController
@AllArgsConstructor
@Tag(name = "Movie Management",
        description = "Complete movie management system including CRUD operations, search, filtering, and user favorites")
public class MovieController {

    private final MovieService movieService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /*    USER ENDPOINTS    */

    @GetMapping("/recent")
    @Operation(
            summary = "Get recent movies",
            description = "Retrieves a list of recently added movies to the platform. Movies are sorted by creation date in descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recent movies retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "null"))
            )
    })
    public ResponseEntity<List<MovieResponseDTO>> getRecentMovies() {
        try {
            List<MovieResponseDTO> movies = movieService.getRecentMovies();
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get movie by ID",
            description = "Retrieves detailed information about a specific movie by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found with the provided ID",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Internal server error\""))
            )
    })
    public ResponseEntity<?> getMovieById(
            @Parameter(description = "Unique identifier of the movie", required = true, example = "1")
            @PathVariable Long id) {
        try {
            MovieResponseDTO movie = movieService.getMovieById(id);
            return ResponseEntity.ok(movie);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search movies by title or description",
            description = "Searches for movies based on a query string that matches movie titles or descriptions. Case-insensitive search."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies matching search query found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during search",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "null"))
            )
    })
    public ResponseEntity<List<MovieResponseDTO>> searchMovies(
            @Parameter(description = "Search query for movie title or description", required = true, example = "Avengers")
            @RequestParam String query) {
        try {
            List<MovieResponseDTO> movies = movieService.searchMovies(query);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/filter/{filterType}/{title}/{sortBy}")
    @Operation(
            summary = "Filter and sort movies",
            description = "Filters movies by type and title with custom sorting options. Supports various filter criteria and sorting mechanisms."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered and sorted movies retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during filtering",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "null"))
            )
    })
    public ResponseEntity<List<MovieResponseDTO>> filterMovies(
            @Parameter(description = "Type of filter to apply", required = true, example = "genre")
            @PathVariable String filterType,
            @Parameter(description = "Title or value to filter by", required = true, example = "Action")
            @PathVariable String title,
            @Parameter(description = "Sorting criteria", required = true, example = "releaseDate")
            @PathVariable String sortBy) {
        try {
            List<MovieResponseDTO> movies = movieService.filterMovies(filterType, title, sortBy);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ============ ADMIN MOVIE ENDPOINTS ============

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new movie (Admin only)",
            description = "Creates a new movie entry in the system. Only accessible by admin users with proper authentication.",
            security = @SecurityRequirement(name = "JWT Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movie creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieCreateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "title": "The Dark Knight",
                                                "description": "Batman faces the Joker in this epic superhero film",
                                                "genre": "Action",
                                                "duration": 152,
                                                "releaseDate": "2008-07-18",
                                                "rating": 9.0,
                                                "language": "English",
                                                "posterUrl": "https://example.com/poster.jpg"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Movie created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error creating movie",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error creating movie: [error details]\""))
            )
    })
    public ResponseEntity<?> createMovie(
            @Parameter(description = "Movie creation request details", required = true)
            @RequestBody MovieCreateRequest request) {
        try {
            MovieResponseDTO response = movieService.createMovie(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating movie: " + e.getMessage());
        }
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update movie details (Admin only)",
            description = "Updates an existing movie's information. Only accessible by admin users with proper authentication.",
            security = @SecurityRequirement(name = "JWT Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movie update details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "title": "The Dark Knight Rises",
                                                "description": "Updated description for the movie",
                                                "genre": "Action",
                                                "duration": 165,
                                                "rating": 8.4,
                                                "language": "English"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found with the provided ID",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error updating movie",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error updating movie: [error details]\""))
            )
    })
    public ResponseEntity<?> updateMovie(
            @Parameter(description = "ID of the movie to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Movie update request details", required = true)
            @RequestBody MovieUpdateRequest request) {
        try {
            MovieResponseDTO response = movieService.updateMovie(id, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating movie: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete movie (Admin only)",
            description = "Permanently deletes a movie from the system by its ID. Only accessible by admin users with proper authentication.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie deleted successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie deleted successfully\""))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found with the provided ID",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error deleting movie",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error deleting movie: [error details]\""))
            )
    })
    public ResponseEntity<?> deleteMovie(
            @Parameter(description = "ID of the movie to delete", required = true, example = "1")
            @PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok("Movie deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting movie: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all movies for admin (Admin only)",
            description = "Retrieves all movies in the system with pagination support. Only accessible by admin users for management purposes.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching movies",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching movies: [error details]\""))
            )
    })
    public ResponseEntity<?> getAllMoviesForAdmin(
            @Parameter(description = "Page number for pagination", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<MovieResponseDTO> movies = movieService.getAllMovies();
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching movies: " + e.getMessage());
        }
    }

    // ============ ADMIN DASHBOARD ENDPOINTS ============

    @GetMapping("/admin/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get admin dashboard statistics (Admin only)",
            description = "Retrieves key statistics for the admin dashboard including total users, movies, bookings, and revenue.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard statistics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminDashboardStats.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching dashboard stats",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching dashboard stats: [error details]\""))
            )
    })
    public ResponseEntity<?> getDashboardStats() {
        try {
            AdminDashboardStats stats = new AdminDashboardStats();
            stats.setTotalUsers(userRepository.count());
            stats.setTotalMovies((long) movieService.getAllMovies().size());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    @Data
    @Schema(description = "Admin dashboard statistics containing key metrics")
    public static class AdminDashboardStats {
        @Schema(description = "Total number of registered users", example = "1250")
        private Long totalUsers;

        @Schema(description = "Total number of movies in the system", example = "450")
        private Long totalMovies;

        @Schema(description = "Total number of bookings made", example = "3200")
        private Long totalBookings;

        @Schema(description = "Total revenue generated", example = "125000.50")
        private Double totalRevenue;
    }

    @Data
    @Schema(description = "Paginated response containing user list and pagination metadata")
    public static class AdminUserResponse {
        @Schema(description = "List of users in the current page")
        private List<UserResponseDTO> users;

        @Schema(description = "Total number of users across all pages", example = "1250")
        private long totalElements;

        @Schema(description = "Total number of pages available", example = "125")
        private int totalPages;

        @Schema(description = "Current page number (zero-based)", example = "0")
        private int currentPage;
    }
}