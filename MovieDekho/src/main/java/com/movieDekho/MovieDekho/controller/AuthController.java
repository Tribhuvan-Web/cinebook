package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.dtos.*;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import com.movieDekho.MovieDekho.service.otpservice.OtpService;
import com.movieDekho.MovieDekho.service.userService.UserService;
import lombok.AllArgsConstructor;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final BrevoEmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody RegisterUserDTO user) {
        try {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setEmail(user.getEmail());
            newUser.setRole("Role_User");
            newUser.setGender(user.getGender());
            newUser.setPhone(user.getPhone());
            userService.registerUser(newUser);
            return ResponseEntity.ok("User registered successfully");
        } catch (UserDetailsAlreadyExist e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email/phone already exist");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        try {
            JwtAuthenticationResponse response = userService.loginUser(loginUserDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/initiate-login")
    public ResponseEntity<?> initiateLogin(@RequestBody LoginInitiationDto request) {
        try {
            String targetEmail;
            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body("Must provide email or phone number");
            }

            if (!userService.userExists(targetEmail)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String otp = otpService.generateOtp(targetEmail);
            emailService.sendOtpEmail(targetEmail, otp);

            return ResponseEntity.ok("OTP sent successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody LoginInitiationDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body("Must provide email or phone number");
            }

            if (!userService.userExists(targetEmail)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String otp = otpService.generateOtp(targetEmail);
            emailService.sendPasswordResetEmail(targetEmail, otp);

            return ResponseEntity.ok("Password reset OTP sent successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body("Must provide email or phone number");
            }

            if (!otpService.validateOtp(targetEmail, request.getOtp())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

            JwtAuthenticationResponse response = userService.loginWithEmail(targetEmail);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDto request) {
        try {
            String targetEmail;

            if (request.hasPhoneNumber()) {
                targetEmail = userService.findEmailByPhone(request.getPhoneNumber());
            } else if (request.hasEmail()) {
                targetEmail = request.getEmail();
            } else {
                return ResponseEntity.badRequest().body("Must provide email or phone number");
            }

            if (!otpService.validateOtp(targetEmail, request.getOtp())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

            userService.resetPassword(targetEmail, request.getNewPassword());

            return ResponseEntity.ok("Password reset successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (InvalidDataAccessApiUsageException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid password format");
        }
    }
}
