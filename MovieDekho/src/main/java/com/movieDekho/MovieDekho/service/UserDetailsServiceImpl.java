package com.movieDekho.MovieDekho.service;

import com.movieDekho.MovieDekho.Config.Utils.UserDetailsImplement;
import com.movieDekho.MovieDekho.dtos.RegisterUser;
import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("NoUserFoundByThisName"+username));
        //Here we are Converting this to Object OF UserDetailsIMPL So Spring Security UnderStand
        return UserDetailsImplement.build(user);
    }
}
