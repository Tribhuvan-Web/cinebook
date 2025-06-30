package com.movieDekho.MovieDekho.config.userImplementation;

import com.movieDekho.MovieDekho.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImplement implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String gender;
    private String role;

    private Collection<? extends GrantedAuthority> authorities;

    // Static factory method to convert User -> UserDetailsImplement
    public static UserDetailsImplement build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImplement(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getRole(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username; // Fixed: It was returning password earlier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Set to true unless you want to expire accounts
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Set to true unless locking is needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Set to true unless you expire credentials
    }

    @Override
    public boolean isEnabled() {
        return true; // Set to true unless user is disabled
    }
}
