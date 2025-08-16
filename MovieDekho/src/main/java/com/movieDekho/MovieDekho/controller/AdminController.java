package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.user.UserResponseDTO;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.repository.BookingRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Profile & Management",
        description = "Admin profile management and administrative functions for authenticated admins")
public class AdminController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @GetMapping("/profile")
    @Operation(
            summary = "Get admin profile",
            description = "Retrieves the current authenticated admin's profile information including personal details and account settings.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin not found\""))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching admin profile",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching profile: [error details]\""))
            )
    })
    public ResponseEntity<?> getAdminProfile(
            @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> adminOpt = userRepository.findByEmailOrPhone(username);

            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                // Verify the user is actually an admin
                if (!"ROLE_ADMIN".equals(admin.getRole()) && !"ADMIN".equals(admin.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: Not an admin user");
                }
                UserResponseDTO response = UserMapper.toUserResponseDTO(admin);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching admin profile: " + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    @Operation(
            summary = "Change admin password",
            description = "Allows the authenticated admin to change their password by providing current and new password.",
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
                    description = "Invalid password data",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "Incorrect current password", value = "\"Current password is incorrect\""),
                            @ExampleObject(name = "Password too short", value = "\"New password must be at least 6 characters long\"")
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Admin not found\""))
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
            Optional<User> adminOpt = userRepository.findByEmailOrPhone(username);

            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();

                // Verify the user is actually an admin
                if (!"ROLE_ADMIN".equals(admin.getRole()) && !"ADMIN".equals(admin.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: Not an admin user");
                }

                if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
                }

                if (request.getNewPassword().length() < 6) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("New password must be at least 6 characters long");
                }

                admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(admin);

                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    @Operation(
            summary = "Get admin dashboard information",
            description = "Retrieves comprehensive dashboard information for admins including statistics and recent activities.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin dashboard information retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminDashboardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching dashboard information",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching dashboard: [error details]\""))
            )
    })
    public ResponseEntity<?> getAdminDashboard(
            @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> adminOpt = userRepository.findByEmailOrPhone(username);

            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();

                // Verify the user is actually an admin
                if (!"ROLE_ADMIN".equals(admin.getRole()) && !"ADMIN".equals(admin.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: Not an admin user");
                }

                AdminDashboardResponse dashboardResponse = new AdminDashboardResponse();
                dashboardResponse.setAdmin(UserMapper.toUserResponseDTO(admin));

                // Get statistics
                long totalUsers = userRepository.count();
                long totalBookings = bookingRepository.count();

                // Get recent users (last 10)
                Pageable recentUsersPageable = PageRequest.of(0, 10, Sort.by("id").descending());
                Page<User> recentUsersPage = userRepository.findAll(recentUsersPageable);
                List<UserResponseDTO> recentUsers = recentUsersPage.getContent().stream()
                        .map(UserMapper::toUserResponseDTO)
                        .collect(Collectors.toList());

                dashboardResponse.setTotalUsers(totalUsers);
                dashboardResponse.setTotalBookings(totalBookings);
                dashboardResponse.setRecentUsers(recentUsers);
                dashboardResponse.setDashboardGeneratedAt(LocalDateTime.now());

                return ResponseEntity.ok(dashboardResponse);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching admin dashboard: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    @Operation(
            summary = "Get all users (Admin only)",
            description = "Retrieves a paginated list of all users in the system. Only accessible by admins.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied: Admin role required\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error fetching users",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error fetching users: [error details]\""))
            )
    })
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> adminOpt = userRepository.findByEmailOrPhone(username);

            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();

                // Verify the user is actually an admin
                if (!"ROLE_ADMIN".equals(admin.getRole()) && !"ADMIN".equals(admin.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: Admin role required");
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<User> usersPage = userRepository.findAll(pageable);

                List<UserResponseDTO> users = usersPage.getContent().stream()
                        .map(UserMapper::toUserResponseDTO)
                        .collect(Collectors.toList());

                AdminUserResponse response = new AdminUserResponse();
                response.setUsers(users);
                response.setTotalElements(usersPage.getTotalElements());
                response.setTotalPages(usersPage.getTotalPages());
                response.setCurrentPage(usersPage.getNumber());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    @GetMapping("/users/search")
    @Operation(
            summary = "Search users (Admin only)",
            description = "Search users by username, email, or phone. Only accessible by admins.",
            security = @SecurityRequirement(name = "JWT Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users search completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Unauthorized\""))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - admin role required",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Access denied: Admin role required\""))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error searching users",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Error searching users: [error details]\""))
            )
    })
    public ResponseEntity<?> searchUsers(
            @Parameter(description = "JWT authentication token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Search query to find users by username, email, or phone", required = true, example = "john")
            @RequestParam String query) {
        try {
            String username = extractUsernameFromToken(authHeader);
            Optional<User> adminOpt = userRepository.findByEmailOrPhone(username);

            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();

                // Verify the user is actually an admin
                if (!"ROLE_ADMIN".equals(admin.getRole()) && !"ADMIN".equals(admin.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: Admin role required");
                }

                // Search users by username or email containing the query
                List<User> foundUsers = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

                List<UserResponseDTO> users = foundUsers.stream()
                        .map(UserMapper::toUserResponseDTO)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(users);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching users: " + e.getMessage());
        }
    }

    public String extractUsernameFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtils.getNameFromJwt(token);
        }
        return null;
    }

    /**
     * Helper method to validate username extracted from token
     */
    private boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() &&
                !"undefined".equals(username) && !"null".equals(username);
    }

    @Data
    @Schema(description = "Request object for updating admin profile information")
    public static class AdminProfileUpdateRequest {
        @Schema(description = "New username for the admin", example = "admin_updated")
        private String username;

        @Schema(description = "Admin's gender", example = "Male", allowableValues = {"Male", "Female", "Other"})
        private String gender;
    }

    @Data
    @Schema(description = "Request object for changing admin password")
    public static class ChangePasswordRequest {
        @Schema(description = "Current password", example = "currentPassword123")
        private String currentPassword;

        @Schema(description = "New password (minimum 6 characters)", example = "newPassword123")
        private String newPassword;
    }

    @Data
    @Schema(description = "Response object containing admin dashboard information")
    public static class AdminDashboardResponse {
        @Schema(description = "Admin profile information")
        private UserResponseDTO admin;

        @Schema(description = "Total number of users in the system", example = "1250")
        private long totalUsers;

        @Schema(description = "Total number of bookings made", example = "3200")
        private long totalBookings;

        @Schema(description = "List of recently registered users")
        private List<UserResponseDTO> recentUsers;

        @Schema(description = "Timestamp when dashboard was generated")
        private LocalDateTime dashboardGeneratedAt;
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
