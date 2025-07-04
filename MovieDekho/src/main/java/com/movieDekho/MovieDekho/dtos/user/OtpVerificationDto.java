package com.movieDekho.MovieDekho.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpVerificationDto {
    private String email;
    private String phoneNumber;
    private String otp;

    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isEmpty();
    }
}