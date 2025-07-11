package com.movieDekho.MovieDekho.dtos.user;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUserDTO {
    private String username; // This will store either email or phone
    private String password;
    
    // Helper methods to support both email and phone login
    public void setEmail(String email) {
        this.username = email;
    }
    
    public void setPhone(String phone) {
        this.username = phone;
    }
    
    public String getUsername() {
        return username;
    }
}
