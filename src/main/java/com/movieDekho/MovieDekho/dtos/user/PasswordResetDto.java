package com.movieDekho.MovieDekho.dtos.user;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordResetDto {
    private String email;
    private String phoneNumber;
    private String otp;
    private String newPassword;

    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isEmpty();
    }
}
