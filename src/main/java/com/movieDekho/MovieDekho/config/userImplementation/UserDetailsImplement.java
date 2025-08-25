package com.movieDekho.MovieDekho.config.userImplementation;

import com.movieDekho.MovieDekho.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
public class UserDetailsImplement implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String gender;
    private String role;
    private Boolean isApproved;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImplement(Long id, String username, String password, String email, 
                                String phone, String gender, String role, Boolean isApproved,
                                Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.isApproved = isApproved;
        this.authorities = authorities;
    }

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
                user.getIsApproved(),
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
        return username; 
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; 
    }

    @Override
    public boolean isEnabled() {
        // Pending admins should not be able to access admin functions
        if ("PENDING_ADMIN".equals(role) || "REJECTED_ADMIN".equals(role)) {
            return false;
        }
        // Regular users are always enabled
        if ("ROLE_USER".equals(role)) {
            return true;
        }
        // Admins must be approved
        if ("ROLE_ADMIN".equals(role)) {
            return isApproved != null && isApproved;
        }
        return true;
    }
}
