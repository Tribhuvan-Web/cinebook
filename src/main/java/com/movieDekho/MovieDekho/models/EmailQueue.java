package com.movieDekho.MovieDekho.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlContent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType emailType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;
    
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    @Column(nullable = false)
    private Integer maxRetries = 3;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime scheduledAt;
    
    @Column
    private LocalDateTime sentAt;
    
    @Column
    private LocalDateTime lastAttemptAt;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(nullable = false)
    private Integer priority = 1;

    @Column
    private Long relatedEntityId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scheduledAt == null) {
            scheduledAt = LocalDateTime.now();
        }
    }
    
    public enum EmailType {
        WELCOME,
        OTP,
        PASSWORD_RESET,
        ADMIN_REGISTRATION_NOTIFICATION,
        ADMIN_APPROVAL_NOTIFICATION,
        BOOKING_CONFIRMATION,
        BOOKING_CANCELLATION,
        GENERAL
    }
    
    public enum EmailStatus {
        PENDING,
        PROCESSING,
        SENT,
        FAILED,
        CANCELLED,
        SCHEDULED
    }
}