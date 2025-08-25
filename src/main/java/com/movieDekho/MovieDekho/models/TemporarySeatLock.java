package com.movieDekho.MovieDekho.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "temporary_seat_locks")
public class TemporarySeatLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)  
    private MovieSlot slot;
    
    @Column(nullable = false)
    private String sessionId; // To identify the user session
    
    @Column(nullable = false)
    private LocalDateTime lockedAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean isActive = true;
}
