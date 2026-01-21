package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.EmailQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
    
    /**
     * Find emails ready to be sent (PENDING or FAILED with retry attempts remaining)
     * ordered by priority (descending) and creation time (ascending)
     */
    @Query("SELECT e FROM EmailQueue e WHERE " +
           "(e.status = 'PENDING' OR (e.status = 'FAILED' AND e.retryCount < e.maxRetries)) " +
           "AND e.scheduledAt <= :now " +
           "ORDER BY e.priority DESC, e.createdAt ASC")
    List<EmailQueue> findEmailsReadyToSend(@Param("now") LocalDateTime now, Pageable pageable);
    
    /**
     * Find emails by status
     */
    List<EmailQueue> findByStatus(EmailQueue.EmailStatus status);
    
    /**
     * Find emails by email type
     */
    List<EmailQueue> findByEmailType(EmailQueue.EmailType emailType);
    
    /**
     * Find emails by recipient
     */
    List<EmailQueue> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    /**
     * Find failed emails that have exceeded max retries
     */
    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'FAILED' AND e.retryCount >= e.maxRetries")
    List<EmailQueue> findPermanentlyFailedEmails();
    
    /**
     * Mark old emails as cancelled (cleanup)
     */
    @Modifying
    @Query("UPDATE EmailQueue e SET e.status = 'CANCELLED' WHERE e.createdAt < :cutoffDate AND e.status IN ('PENDING', 'FAILED')")
    int cancelOldEmails(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count emails by status
     */
    long countByStatus(EmailQueue.EmailStatus status);
    
    /**
     * Count emails by email type and status
     */
    long countByEmailTypeAndStatus(EmailQueue.EmailType emailType, EmailQueue.EmailStatus status);
    
    /**
     * Find emails scheduled for future sending
     */
    @Query("SELECT e FROM EmailQueue e WHERE e.scheduledAt > :now AND e.status = 'SCHEDULED'")
    List<EmailQueue> findScheduledEmails(@Param("now") LocalDateTime now);
    
    /**
     * Find emails currently being processed (to detect stuck emails)
     */
    @Query("SELECT e FROM EmailQueue e WHERE e.status = 'PROCESSING' AND e.lastAttemptAt < :cutoffTime")
    List<EmailQueue> findStuckProcessingEmails(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Reset stuck emails back to pending
     */
    @Modifying
    @Query("UPDATE EmailQueue e SET e.status = 'PENDING' WHERE e.status = 'PROCESSING' AND e.lastAttemptAt < :cutoffTime")
    int resetStuckEmails(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count total emails sent in a time period
     */
    @Query("SELECT COUNT(e) FROM EmailQueue e WHERE e.status = 'SENT' AND e.sentAt BETWEEN :startTime AND :endTime")
    long countSentEmailsInPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}