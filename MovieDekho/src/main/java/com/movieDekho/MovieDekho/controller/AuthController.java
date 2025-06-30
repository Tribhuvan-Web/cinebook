package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.dtos.LoginUserDTO;
import com.movieDekho.MovieDekho.dtos.RegisterUserDTO;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.service.userService.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/api/save")
    public ResponseEntity<?> saveUser(@RequestBody RegisterUserDTO user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());
        newUser.setEmail(user.getEmail());
        newUser.setRole("Role_User");
        newUser.setGender(user.getGender());
        newUser.setPhone(user.getPhone());
        userService.saveUser(newUser);
        return ResponseEntity.ok("User saved successfully");
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        try {
            JwtAuthenticationResponse response = userService.loginUser(loginUserDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

}
