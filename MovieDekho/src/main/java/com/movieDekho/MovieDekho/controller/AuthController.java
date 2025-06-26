package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.dtos.RegisterUser;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/api/save")
    private ResponseEntity<?> saveUser(@RequestBody RegisterUser user) {
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
}
