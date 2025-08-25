package com.movieDekho.MovieDekho.dtos.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyTicketRequest {
    @NotBlank(message = "QR code is required")
    private String qrCode;
}
