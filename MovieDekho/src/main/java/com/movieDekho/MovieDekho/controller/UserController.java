package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.dtos.user.UserResponseDTO;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.service.userService.FavoritesService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User Profile & Favorites", 
     description = "User profile management and movie favorites system for authenticated users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FavoritesService favoritesService;

    @GetMapping("/profile")
    @Operation(
        summary = "Get user profile",
        description = "Retrieves the current authenticated user's profile information including personal details and account settings.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"User not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error fetching user profile",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching profile: [error details]\""))
        )
    })
    public ResponseEntity<?> getUserProfile(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> userOpt = userRepository.findByEmailOrPhone(username);

            if (userOpt.isPresent()) {
                UserResponseDTO response = UserMapper.toUserResponseDTO(userOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching profile: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    @Operation(
        summary = "Update user profile",
        description = "Updates the current authenticated user's profile information including username and gender.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Profile updated successfully\""))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"User not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error updating profile",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error updating profile: [error details]\""))
        )
    })
    public ResponseEntity<?> updateProfile(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Profile update request containing new user information", required = true)
        @RequestBody UserProfileUpdateRequest request) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> userOpt = userRepository.findByEmailOrPhone(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (request.getUsername() != null && !user.getUsername().isEmpty()) {
                    user.setUsername(request.getUsername());
                }

                if (request.getGender() != null && !request.getGender().isEmpty()) {
                    user.setGender(request.getGender());
                }

                userRepository.save(user);
                return ResponseEntity.ok("Profile updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    @Operation(
        summary = "Change user password",
        description = "Allows authenticated users to change their account password by providing current password and new password. New password must be at least 6 characters long.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Password changed successfully\""))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - current password incorrect or new password too short",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "Incorrect current password", value = "\"Current password is incorrect\""),
                @ExampleObject(name = "Password too short", value = "\"New password must be at least 6 characters long\"")
            })
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"User not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error changing password",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error changing password: [error details]\""))
        )
    })
    public ResponseEntity<?> changePassword(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Password change request containing current and new password", required = true)
        @RequestBody ChangePasswordRequest request) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> userOpt = userRepository.findByEmailOrPhone(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
                }

                if (request.getNewPassword().length() < 6) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("New password must be at least 6 characters long");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }

    @GetMapping("/favorites")
    @Operation(
        summary = "Get favorite movies",
        description = "Retrieves all movies that the authenticated user has marked as favorites.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Favorite movies retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponseDTO.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error fetching favorites",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching favorites: [error details]\""))
        )
    })
    public ResponseEntity<?> getFavoriteMovies(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader) {
        try {
            String username = extractUsernameFromToken(authHeader);
            List<MovieResponseDTO> favorites = favoritesService.getFavoriteMovies(username);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching favorites: " + e.getMessage());
        }
    }

    @PostMapping("/favorites/{movieId}")
    @Operation(
        summary = "Add movie to favorites",
        description = "Adds a specific movie to the authenticated user's favorites list.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Movie added to favorites successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie added to favorites successfully\""))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Movie already in favorites or invalid movie ID",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie already in favorites\""))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Movie not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error adding to favorites",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error adding to favorites: [error details]\""))
        )
    })
    public ResponseEntity<?> addToFavorites(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of the movie to add to favorites", required = true, example = "1")
        @PathVariable Long movieId) {
        try {
            String username = extractUsernameFromToken(authHeader);
            String message = favoritesService.addToFavorites(username, movieId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding to favorites: " + e.getMessage());
        }
    }

    @DeleteMapping("/favorites/{movieId}")
    @Operation(
        summary = "Remove movie from favorites",
        description = "Removes a specific movie from the authenticated user's favorites list.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Movie removed from favorites successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie removed from favorites successfully\""))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Movie not in favorites or invalid movie ID",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not in favorites\""))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Movie not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Movie not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error removing from favorites",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error removing from favorites: [error details]\""))
        )
    })
    public ResponseEntity<?> removeFromFavorites(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of the movie to remove from favorites", required = true, example = "1")
        @PathVariable Long movieId) {
        try {
            String username = extractUsernameFromToken(authHeader);
            String message = favoritesService.removeFromFavorites(username, movieId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing from favorites: " + e.getMessage());
        }
    }

    @DeleteMapping("/favorites/deleteall")
    @Operation(
        summary = "Remove all favorite movies",
        description = "Removes all movies from the authenticated user's favorites list.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All favorites removed successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Remove all favorites\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Error deleting all favorites",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error deleting all favorites: [error details]\""))
        )
    })
    public ResponseEntity<?> removeAllFavorites(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader) {
        try {
            String username = extractUsernameFromToken(authHeader);
            favoritesService.removeAllFavorites(username);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Remove all favorites");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error deleting all favorites: " + e.getMessage());
        }
    }

    @GetMapping("/bookings")
    @Operation(
        summary = "Get booking history",
        description = "Retrieves the booking history for the authenticated user. This feature is currently under development.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Booking history feature acknowledgment",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Booking history feature coming soon\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error fetching booking history",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching booking history: [error details]\""))
        )
    })
    public ResponseEntity<?> getBookingHistory(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok("Booking history feature coming soon");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching booking history: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(
        summary = "Delete user account",
        description = "Permanently deletes the authenticated user's account and all associated data. This action cannot be undone.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User account deleted successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"User deleted successfully\""))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"User not found\""))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error deleting user account",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error deleting user: [error details]\""))
        )
    })
    public ResponseEntity<?> deleteUser(
        @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = userRepository.findByEmailOrPhone(extractUsernameFromToken(authHeader));
        userRepository.deleteById(userOpt.get().getId());
        return ResponseEntity.ok("User deleted successfully");
    }

    public String extractUsernameFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtils.getNameFromJwt(token);
    }

    @Data
    @Schema(description = "Request object for updating user profile information")
    public static class UserProfileUpdateRequest {
        @Schema(description = "New username for the user", example = "john_doe_updated")
        private String username;
        
        @Schema(description = "New email address for the user", example = "john.doe.updated@example.com")
        private String email;
        
        @Schema(description = "New phone number for the user", example = "+1234567890")
        private String phone;
        
        @Schema(description = "User's gender", example = "Male", allowableValues = {"Male", "Female", "Other"})
        private String gender;
    }

    @Data
    @Schema(description = "Request object for changing user password")
    public static class ChangePasswordRequest {
        @Schema(description = "Current password of the user", example = "currentPassword123", required = true)
        private String currentPassword;
        
        @Schema(description = "New password for the user (minimum 6 characters)", example = "newPassword123", required = true, minLength = 6)
        private String newPassword;
    }

    @Data
    @Schema(description = "Response object containing user dashboard information")
    public static class UserDashboardResponse {
        @Schema(description = "User profile information")
        private UserResponseDTO user;
        
        @Schema(description = "List of recently viewed movies")
        private List<MovieResponseDTO> recentMovies;
        
        @Schema(description = "Total count of favorite movies", example = "15")
        private int favoriteMoviesCount;
        
        @Schema(description = "List of recently added favorite movies")
        private List<MovieResponseDTO> recentFavorites;
    }
}
