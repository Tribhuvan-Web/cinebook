package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.dtos.user.*;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import com.movieDekho.MovieDekho.service.otpservice.OtpService;
import com.movieDekho.MovieDekho.service.userService.UserService;
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

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/auth")
@RestController
@AllArgsConstructor
@Tag(name = "Authentication & Authorization", description = "Complete authentication system including user registration, login, OTP verification, password reset, and admin approval workflows")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final BrevoEmailService emailService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with USER role. User will be immediately activated and can start using the application.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterUserDTO.class), examples = @ExampleObject(name = "User Registration Example", value = """
            {
                "username": "Amit raj",
                "email": "Amitraj@example.com",
                "password": "SecurePassword123!",
                "phone": "+911234567890",
                "gender": "MALE"
            }
            """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User registered successfully\"}"))),
            @ApiResponse(responseCode = "409", description = "Email or phone number already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Email/phone already exist\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid user data provided\"}")))
    })
    public ResponseEntity<?> saveUser(
            @Parameter(description = "User registration information", required = true) @RequestBody RegisterUserDTO user) {
        try {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setEmail(user.getEmail());
            newUser.setRole("ROLE_USER");
            newUser.setGender(user.getGender());
            newUser.setPhone(user.getPhone());
            userService.registerUser(newUser);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (UserDetailsAlreadyExist e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email/phone already exist"));
        }
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register a new admin (requires approval)", description = "Submits an admin registration request. The user will have PENDING_ADMIN role until approved by super admin. An email of verification notification is sent to the super admin.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin registration details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterUserDTO.class), examples = @ExampleObject(name = "Admin Registration Example", value = """
            {
                "username": "admin_user",
                "email": "admin@moviedekho.com",
                "password": "AdminPassword123!",
                "phone": "+911234567890",
                "gender": "MALE"
            }
            """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin registration request submitted successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Admin registration request submitted. Please wait for approval from the super admin.\"}"))),
            @ApiResponse(responseCode = "409", description = "Email or phone already exists, or user not authorized for admin role", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"You are not authorized for admin\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid admin data provided\"}")))
    })
    public ResponseEntity<?> saveAdmin(
            @Parameter(description = "Admin registration information", required = true) @RequestBody RegisterUserDTO user) {
        try {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setEmail(user.getEmail());
            newUser.setRole("PENDING_ADMIN");
            newUser.setGender(user.getGender());
            newUser.setPhone(user.getPhone());
            newUser.setIsApproved(false);
            newUser.setRequestedAt(java.time.LocalDateTime.now());

            User savedUser = userService.registerUser(newUser);

            emailService.sendAdminRegistrationNotification(
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getId());

            return ResponseEntity.ok(Map.of("message",
                    "Admin registration request submitted. Please wait for approval from the super admin."));
        } catch (UserDetailsAlreadyExist e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You have already submitted an admin request"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login with username/email and password", description = "Authenticates user with credentials and returns JWT token for subsequent API calls. Supports login with either username or email.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginUserDTO.class), examples = {
            @ExampleObject(name = "Login with Email", value = """
                    {
                        "email": "RitikJaiswal@example.com",
                        "password": "SecurePassword123!"
                    }
                    """),
            @ExampleObject(name = "Login with Phone", value = """
                    {
                        "phone": "+91916221122",
                        "password": "SecurePassword123!"
                    }
                    """)
    })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthenticationResponse.class), examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "username": "Ritik jaiswal",
                        "email": "Amitraj@example.com",
                        "role": "ROLE_USER"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid credentials\"}"))),
            @ApiResponse(responseCode = "400", description = "Missing required fields", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Username/email and password are required\"}")))
    })
    public ResponseEntity<?> loginUser(
            @Parameter(description = "User login credentials", required = true) @RequestBody LoginUserDTO loginUserDTO) {
        try {
            JwtAuthenticationResponse response = userService.loginUser(loginUserDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/initiate-login")
    @Operation(summary = "Initiate OTP-based login", description = "Sends an OTP to user's email for passwordless login. User can provide either email or phone number to initiate the process.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email or phone number for OTP login", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginInitiationDto.class), examples = {
            @ExampleObject(name = "Login with Email", value = """
                    {
                        "email": "Amitraj@example.com"
                    }
                    """),
            @ExampleObject(name = "Login with Phone", value = """
                    {
                        "phoneNumber": "+1234567890"
                    }
                    """)
    })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully to email", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"OTP sent successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found with provided email/phone", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"User not found\"}"))),
            @ApiResponse(responseCode = "400", description = "Neither email nor phone number provided", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Must provide email or phone number\"}")))
    })
    public ResponseEntity<?> initiateLogin(
            @Parameter(description = "Email or phone number for OTP login", required = true) @RequestBody LoginInitiationDto request) {
        try {
            String targetEmail;
            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Must provide email or phone number"));
            }

            if (!userService.userExists(targetEmail)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            String otp = otpService.generateOtp(targetEmail);
            emailService.sendOtpEmail(targetEmail, otp);

            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset process", description = "Sends a password reset OTP to user's email. User can provide either email or phone number to initiate the reset process.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email or phone number for password reset", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginInitiationDto.class), examples = {
            @ExampleObject(name = "Reset with Email", value = """
                    {
                        "email": "Amitraj@example.com"
                    }
                    """),
            @ExampleObject(name = "Reset with Phone", value = """
                    {
                        "phoneNumber": "+1234567890"
                    }
                    """)
    })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset OTP sent successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Password reset OTP sent successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found with provided email/phone", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"User not found\"}"))),
            @ApiResponse(responseCode = "400", description = "Neither email nor phone number provided", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Must provide email or phone number\"}")))
    })
    public ResponseEntity<?> forgotPassword(
            @Parameter(description = "Email or phone number for password reset", required = true) @RequestBody LoginInitiationDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Must provide email or phone number"));
            }

            if (!userService.userExists(targetEmail)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            String otp = otpService.generateOtp(targetEmail);
            emailService.sendPasswordResetEmail(targetEmail, otp);

            return ResponseEntity.ok(Map.of("message", "Password reset OTP sent successfully"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and complete login", description = "Verifies the OTP sent for login and returns a JWT token for authentication. This completes the passwordless login process.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OTP verification details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = OtpVerificationDto.class), examples = {
            @ExampleObject(name = "Verify OTP with Email", value = """
                    {
                        "email": "Amitraj@example.com",
                        "otp": "123456"
                    }
                    """),
            @ExampleObject(name = "Verify OTP with Phone", value = """
                    {
                        "phoneNumber": "+1234567890",
                        "otp": "123456"
                    }
                    """)
    })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully, JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthenticationResponse.class), examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "username": "Ritik jaiswal",
                        "email": "Amitraj@example.com",
                        "role": "ROLE_USER"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid OTP\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"User not found\"}"))),
            @ApiResponse(responseCode = "400", description = "Missing email/phone or OTP", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Must provide email or phone number\"}")))
    })
    public ResponseEntity<?> verifyOtp(
            @Parameter(description = "OTP verification details", required = true) @RequestBody OtpVerificationDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Must provide email or phone number"));
            }

            if (!otpService.validateOtp(targetEmail, request.getOtp())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid OTP"));
            }

            JwtAuthenticationResponse response = userService.loginWithEmail(targetEmail);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password with OTP", description = "Resets the user's password after verifying the OTP sent for password reset. The OTP must be valid and not expired.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Password reset details including OTP and new password", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordResetDto.class), examples = {
            @ExampleObject(name = "Reset Password with Email", value = """
                    {
                        "email": "Amitraj@example.com",
                        "otp": "123456",
                        "newPassword": "NewSecurePassword123!"
                    }
                    """),
            @ExampleObject(name = "Reset Password with Phone", value = """
                    {
                        "phoneNumber": "+1234567890",
                        "otp": "123456",
                        "newPassword": "NewSecurePassword123!"
                    }
                    """)
    })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Password reset successfully\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid OTP\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"User not found\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid password format or missing required fields", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid password format\"}")))
    })
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Password reset information including OTP and new password", required = true) @RequestBody PasswordResetDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Must provide email or phone number"));
            }

            if (!otpService.validateOtp(targetEmail, request.getOtp())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid OTP"));
            }

            userService.resetPassword(targetEmail, request.getNewPassword());

            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        } catch (InvalidDataAccessApiUsageException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid password format"));
        }
    }

    @GetMapping("/username")
    @Operation(summary = "Get current user's username", description = "Retrieves the username of the currently authenticated user from the JWT token. Requires valid authentication token in the Authorization header.", security = @SecurityRequirement(name = "JWT Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username retrieved successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"username\": \"Reyansh11241\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid credentials\"}"))),
            @ApiResponse(responseCode = "403", description = "Token expired or malformed", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid credentials\"}")))
    })
    public ResponseEntity<?> getName(
            @Parameter(description = "JWT token in the format: Bearer <token>", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
                String username = jwtUtils.getDisplayNameFromJwt(authHeader);
                return ResponseEntity.ok(Map.of("username", username));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }
}
