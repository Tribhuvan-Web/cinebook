package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.dtos.LoginInitiationDto;
import com.movieDekho.MovieDekho.dtos.LoginUserDTO;
import com.movieDekho.MovieDekho.dtos.OTPVerificationDTO;
import com.movieDekho.MovieDekho.dtos.RegisterUserDTO;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import com.movieDekho.MovieDekho.service.otpservice.OtpService;
import com.movieDekho.MovieDekho.service.userService.UserService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        if (!userService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OTPVerificationDTO request) {
        // 1. Validate OTP
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }

        // 2. Authenticate and generate JWT
        try {
            JwtAuthenticationResponse response = userService.loginWithOtp(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }
}
