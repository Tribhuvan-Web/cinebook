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

    private UserDetailsServiceImpl detailsService;

    @Bean
    public JwtAuthFilter getJwtAuthFilter() {
        return new JwtAuthFilter();
    }

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
                                // Public authentication endpoints
                                .requestMatchers("/api/auth/**").permitAll()
                                
                                // Super Admin endpoints - public access via email links
                                .requestMatchers("/api/super-admin/**").permitAll()
                                
                                // Admin endpoints - must be defined before general patterns
                                .requestMatchers("/movies/admin/**").hasRole("USER")
                                .requestMatchers("/api/admin/**").hasRole("USER")
                                .requestMatchers("/api/seats/admin/**").hasRole("USER")
                                .requestMatchers("/api/slots/admin/**").hasRole("USER")

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

                                // User endpoints
                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                                
                                // Seat endpoints for authenticated users
                                .requestMatchers("/api/seats/slot/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/seats/{seatId}").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/seats/{seatId}/availability").hasAnyRole("USER", "ADMIN")

                                // All other requests require authentication
                                .anyRequest().authenticated());

        security.authenticationProvider(daoAuthenticationProvider());
        security.addFilterBefore(getJwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }
}
