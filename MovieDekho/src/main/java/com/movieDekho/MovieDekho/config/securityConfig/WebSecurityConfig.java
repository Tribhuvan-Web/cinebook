package com.movieDekho.MovieDekho.config.securityConfig;

import com.movieDekho.MovieDekho.config.jwtUtils.JwtAuthFilter;
import com.movieDekho.MovieDekho.service.userService.UserDetailsServiceImpl;

import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl detailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager getAuthenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/api/auth/**").permitAll()

                                // Swagger UI endpoints
                                .requestMatchers("/api/swagger-ui.html").permitAll()
                                .requestMatchers("/api/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/docs/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-resources/**").permitAll()
                                .requestMatchers("/webjars/**").permitAll()

                                // Super Admin endpoints - public access via email links
                                .requestMatchers("/api/super-admin/**").permitAll()

                                // Admin endpoints - must be defined before general patterns
                                .requestMatchers("/movies/admin/**").hasRole("ADMIN")   
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/seats/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/slots/admin/**").hasRole("ADMIN")

                                // Public movie endpoints
                                .requestMatchers("/movies/recent").permitAll()
                                .requestMatchers("/movies/{id}").permitAll()
                                .requestMatchers("/movies/search").permitAll()
                                .requestMatchers("/movies/filter/**").permitAll()

                                // Public movie slot endpoints
                                .requestMatchers("/api/slots/movie/**").permitAll()
                                .requestMatchers("/api/slots/{slotId}").permitAll()
                                .requestMatchers("/api/slots/available").permitAll()
                                .requestMatchers("/api/slots/theater/**").permitAll()
                                .requestMatchers("/api/slots/date-range").permitAll()
                                .requestMatchers("/api/slots/search").permitAll()

                                // Public seat viewing endpoints - anyone can view seats
                                .requestMatchers("/api/seats/slot/**").permitAll()
                                .requestMatchers("/api/seats/{seatId}").permitAll()
                                .requestMatchers("/api/seats/{seatId}/availability").permitAll()

                                // User endpoints
                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                                // Booking endpoints
                                .requestMatchers("/api/bookings/admin/**").hasRole("ADMIN")
                                
                                // PUBLIC booking endpoints (no auth required) - seat selection & availability
                                .requestMatchers("/api/bookings/select-seats").permitAll()
                                .requestMatchers("/api/bookings/seats/check-availability").permitAll()
                                .requestMatchers("/api/bookings/release-seats").permitAll()
                                
                                // PROTECTED booking endpoints (auth required) - payment & user operations
                                .requestMatchers("/api/bookings/payment").hasRole("USER")
                                .requestMatchers("/api/bookings/user/**").hasRole("USER")
                                .requestMatchers("/api/bookings/{bookingId}").hasRole("USER")
                                .requestMatchers("/api/bookings/{bookingId}/cancel").hasRole("USER")
                                .requestMatchers("/api/bookings/**").hasRole("USER") // Catch-all for any remaining booking endpoints
                                
                                // All other requests require authentication
                                .anyRequest().authenticated());

        security.authenticationProvider(daoAuthenticationProvider());
        security.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }
}
