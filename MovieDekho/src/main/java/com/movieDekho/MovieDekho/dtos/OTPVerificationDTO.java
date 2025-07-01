package com.movieDekho.MovieDekho.dtos;

import lombok.Data;

@Data
public class OTPVerificationDTO {
    private String email;
    private String otp;
}
