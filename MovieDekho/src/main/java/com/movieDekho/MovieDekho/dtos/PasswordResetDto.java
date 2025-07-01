package com.movieDekho.MovieDekho.dtos;

import lombok.Data;

@Data
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
