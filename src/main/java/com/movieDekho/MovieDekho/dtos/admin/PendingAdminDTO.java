package com.movieDekho.MovieDekho.dtos.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingAdminDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private LocalDateTime requestedAt;
    private String status; // PENDING_ADMIN, APPROVED, REJECTED
}
