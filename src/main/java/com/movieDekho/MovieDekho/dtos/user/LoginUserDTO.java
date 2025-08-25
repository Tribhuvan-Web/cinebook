package com.movieDekho.MovieDekho.dtos.user;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUserDTO {
    @Getter
    private String username;
    private String password;
    
    public void setEmail(String email) {
        this.username = email;
    }
    
    public void setPhone(String phone) {
        this.username = phone;
    }

}
