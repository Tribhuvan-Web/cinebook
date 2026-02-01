package com.movieDekho.MovieDekho.service.userService;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.config.userImplementation.UserDetailsImplement;
import com.movieDekho.MovieDekho.dtos.user.LoginUserDTO;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.service.emailService.ResilientEmailService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private ResilientEmailService resilientEmailService; 
    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;

    @Transactional
    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserDetailsAlreadyExist("Email already in use");
        }

        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new UserDetailsAlreadyExist("Phone number already in use");
        }

        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        
        if ("ROLE_USER".equals(user.getRole()) || 
            ("ROLE_ADMIN".equals(user.getRole()) && user.getIsApproved())) {
            resilientEmailService.sendWelcomeEmail(savedUser);
        }
        
        return savedUser;
    }

    @Deprecated
    public void sendWelcomeEmail(User user) {
        resilientEmailService.sendWelcomeEmail(user);
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
            throw new BadCredentialsException("Invalid email/phone or password");
        }
    }

    public User findByUsername(String emailOrPhone) {
        Optional<User> user = userRepository.findByEmail(emailOrPhone);
        if (user.isPresent()) {
            return user.get();
        }
        
        user = userRepository.findByPhone(emailOrPhone);
        if (user.isPresent()) {
            return user.get();
        }
        
        throw new UsernameNotFoundException("User not found with email/phone: " + emailOrPhone);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String findEmailByPhone(String phoneNumber) {
        return userRepository.findByPhone(phoneNumber)
                .map(User::getEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phoneNumber));
    }

    public JwtAuthenticationResponse loginWithEmail(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((UserDetailsImplement) userDetails);

        return new JwtAuthenticationResponse(jwt);
    }


    public JwtAuthenticationResponse loginWithOtp(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken((UserDetailsImplement) userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }


}
