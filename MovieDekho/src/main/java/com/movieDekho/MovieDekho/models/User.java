package com.movieDekho.MovieDekho.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    private String gender;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private Boolean isApproved = true; // For users, always true; for admins, requires approval

    private String approvedBy; // Who approved this admin (if applicable)

    @Column
    private java.time.LocalDateTime requestedAt; // When admin registration was requested

    @Column
    private java.time.LocalDateTime approvedAt; // When admin was approved
}