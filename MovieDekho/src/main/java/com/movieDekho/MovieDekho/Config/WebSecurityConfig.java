package com.movieDekho.MovieDekho.Config;

import com.movieDekho.MovieDekho.Config.Utils.JwtAuthFilter;
import com.movieDekho.MovieDekho.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
//@AllArgsConstructor
public class WebSecurityConfig {
    @Autowired
    private UserDetailsServiceImpl detailsService;


    @Bean
    public JwtAuthFilter getJwtAuthFilter() {
        return new JwtAuthFilter();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setUserDetailsService(detailsService);
        provider.setPasswordEncoder(passwordEncoder() );
        return provider;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/api/save").permitAll()
                                .requestMatchers("/save/movie").hasRole("ADMIN")
                                .anyRequest().authenticated()
                );
        //Run Custom Filter Before inbuild
        security.authenticationProvider(daoAuthenticationProvider());
        security.addFilterBefore(getJwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);


        return security.build();
    }
}
