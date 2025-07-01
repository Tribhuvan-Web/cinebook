package com.movieDekho.MovieDekho.service.userService;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.config.userImplementation.UserDetailsImplement;
import com.movieDekho.MovieDekho.dtos.LoginUserDTO;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;

    public void registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserDetailsAlreadyExist("Email already in use");
        }
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new UserDetailsAlreadyExist("Number already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public JwtAuthenticationResponse loginUser(@RequestBody LoginUserDTO user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImplement userDetailsImplement = (UserDetailsImplement) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetailsImplement);
            return new JwtAuthenticationResponse(jwt);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid mobile/email or password");
        }

    }

    public JwtAuthenticationResponse loginWithOtp(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Manually authenticate user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT
        String jwt = jwtUtils.generateToken((UserDetailsImplement) userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
