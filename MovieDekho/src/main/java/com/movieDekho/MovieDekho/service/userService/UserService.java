package com.movieDekho.MovieDekho.service.userService;

import com.movieDekho.MovieDekho.Config.jwtUtils.JwtAuthenticationResponse;
import com.movieDekho.MovieDekho.Config.jwtUtils.JwtUtils;
import com.movieDekho.MovieDekho.Config.userImplementation.UserDetailsImplement;
import com.movieDekho.MovieDekho.dtos.LoginUserDTO;
import com.movieDekho.MovieDekho.dtos.RegisterUserDTO;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl detailsService;

    public String saveUser(User user) {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();

        if (!userExists) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return "Data Entered";
        }
        return "Duplicate Email";
    }


    public JwtAuthenticationResponse loginUser(LoginUserDTO user){


        try {
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImplement userDetailsImplement=(UserDetailsImplement) authentication.getPrincipal();
            String jwt= jwtUtils.generateToken(userDetailsImplement);

            return new JwtAuthenticationResponse(jwt);
        } catch (AuthenticationException e) {

            //e.printStackTrace();
            return new JwtAuthenticationResponse("Bad Credentials") ;
        }
    }

}
