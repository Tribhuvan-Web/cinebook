package com.movieDekho.MovieDekho.dtos.user;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String gender;
}
