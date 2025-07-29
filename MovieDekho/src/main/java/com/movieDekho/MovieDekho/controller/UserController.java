package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.movie.MovieResponseDTO;
import com.movieDekho.MovieDekho.dtos.user.UserResponseDTO;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.util.UserMapper;
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
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
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
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
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
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
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
    public ResponseEntity<?> getFavoriteMovies(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok("Favorites feature coming soon");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching favorites: " + e.getMessage());
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getBookingHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok("Booking history feature coming soon");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching booking history: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = userRepository.findByEmailOrPhone(extractUsernameFromToken(authHeader));
        userRepository.deleteById(userOpt.get().getId());
        return ResponseEntity.ok("User deleted successfully");
    }

    private String extractUsernameFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtils.getNameFromJwt(token);
    }


    @Data
    public static class UserProfileUpdateRequest {
        private String username;
        private String email;
        private String phone;
        private String gender;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    public static class UserDashboardResponse {
        private UserResponseDTO user;
        private List<MovieResponseDTO> recentMovies;
    }
}
