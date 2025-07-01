package com.movieDekho.MovieDekho.service.userService;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.config.userImplementation.UserDetailsImplement;
import com.movieDekho.MovieDekho.dtos.LoginUserDTO;
import com.movieDekho.MovieDekho.exception.UserDetailsAlreadyExist;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import jakarta.transaction.Transactional;
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
    private BrevoEmailService brevoEmailService;
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
        sendWelcomeEmail(user);
        userRepository.save(user);
    }

    public void sendWelcomeEmail(User user) {
        try {
            String subject = "🎬 Welcome to MovieDekho – Your Gateway to Blockbuster Entertainment!";
            String content = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "  <style>"
                    + "    body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }"
                    + "    .header { background-color: #0d253f; padding: 30px; text-align: center; }"
                    + "    .header h1 { color: #fff; margin: 0; font-size: 24px; }"
                    + "    .content { padding: 30px; background-color: #f8f9fa; }"
                    + "    .features { margin: 20px 0; }"
                    + "    .feature-item { display: flex; align-items: flex-start; margin-bottom: 10px; }"
                    + "    .feature-icon { margin-right: 10px; font-size: 18px; }"
                    + "    .cta-button { display: inline-block; padding: 12px 30px; background-color: #01b4e4; "
                    + "                 color: white; text-decoration: none; border-radius: 5px; font-weight: bold; "
                    + "                 margin: 20px 0; }"
                    + "    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 0.9em; "
                    + "             background-color: #e9ecef; }"
                    + "    .highlight { color: #01b4e4; font-weight: bold; }"
                    + "    .signature { font-family: 'Georgia', serif; font-size: 18px; font-weight: bold; "
                    + "                text-align: center; margin: 20px 0; }"
                    + "  </style>"
                    + "</head>"
                    + "<body>"
                    + "  <div class='header'>"
                    + "    <h1>MovieDekho</h1>"
                    + "  </div>"
                    + "  <div class='content'>"
                    + "    <h2>Hi " + user.getUsername() + ",</h2>"
                    + "    <p>Welcome to MovieDekho – where movie magic begins! 🍿</p>"
                    + "    <p>We're thrilled to have you onboard.</p>"
                    + "    "
                    + "    <p>With MovieDekho, you can:</p>"
                    + "    <div class='features'>"
                    + "      <div class='feature-item'><span class='feature-icon'>✅</span> Discover the latest movie releases</div>"
                    + "      <div class='feature-item'><span class='feature-icon'>✅</span> Book tickets instantly with ease</div>"
                    + "      <div class='feature-item'><span class='feature-icon'>✅</span> Enjoy exclusive offers and deals</div>"
                    + "      <div class='feature-item'><span class='feature-icon'>✅</span> Choose your favorite seats at top theatres near you</div>"
                    + "    </div>"
                    + "    "
                    + "    <p>🎟 Your entertainment journey starts now.</p>"
                    + "    <p>Start booking and experience cinema like never before.</p>"
                    + "    "
                    + "    <div style='text-align: center;'>"
                    + "      <a href='https://yourmoviedekhoapp.com/explore' class='cta-button'>Start Exploring Movies</a>"
                    + "    </div>"
                    + "    "
                    + "    <p>If you have any questions or need help, we're just an email away at "
                    + "       <span class='highlight'>support@moviedekho.com</span>.</p>"
                    + "    "
                    + "    <div class='signature'>Lights. Camera. Action.</div>"
                    + "    "
                    + "    <p style='text-align: center; font-weight: bold;'>Team MovieDekho</p>"
                    + "  </div>"
                    + "  <div class='footer'>"
                    + "    <p>© 2023 MovieDekho. All rights reserved.</p>"
                    + "    <p>123 Cinema Street, Movieville | <a href='https://moviedekho.com'>www.moviedekho.com</a></p>"
                    + "    <p>You're receiving this email because you created an account with MovieDekho.</p>"
                    + "  </div>"
                    + "</body>"
                    + "</html>";

            brevoEmailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
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

        // Generate JWT
        String jwt = jwtUtils.generateToken((UserDetailsImplement) userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
