package com.movieDekho.MovieDekho.dtos.admin;

import lombok.Data;

@Data
public class AdminApprovalRequestDTO {
    private Long userId;
    private String action; // "APPROVE" or "REJECT"
    private String reason; // Optional reason for rejection
}
