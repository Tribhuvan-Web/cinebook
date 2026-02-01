package com.movieDekho.MovieDekho.service.emailService;

import com.movieDekho.MovieDekho.models.EmailQueue;
import com.movieDekho.MovieDekho.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResilientEmailService {

    private final EmailQueueService emailQueueService;
    private final EmailProcessorService emailProcessorService;

    public void sendWelcomeEmail(User user) {
        String subject = "🎬 Welcome to CineBook – Your Gateway to Blockbuster Entertainment!";
        String content = buildWelcomeEmailContent(user);

        emailQueueService.queueEmail(
            user.getEmail(), 
            subject, 
            content, 
            EmailQueue.EmailType.WELCOME, 
            user.getId()
        );
        
        log.info("Welcome email queued for user: {} ({})", user.getUsername(), user.getEmail());
    }

    public void sendOtpEmail(String recipientEmail, String otpCode) {
        String subject = "Your Login OTP Code";
        String content = buildOtpEmailContent(otpCode);
        
        try {
            // OTP emails should be sent immediately due to time sensitivity
            boolean sent = emailProcessorService.sendImmediateEmail(
                recipientEmail, 
                subject, 
                content, 
                EmailQueue.EmailType.OTP
            );
            
            if (!sent) {
                // If immediate sending fails, queue with high priority
                emailQueueService.queueEmail(
                    recipientEmail, 
                    subject, 
                    content, 
                    EmailQueue.EmailType.OTP, 
                    null
                );
                log.warn("OTP email queued due to sending failure for: {}", recipientEmail);
            }
            
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", recipientEmail, e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    /**
     * Send password reset email with immediate attempt and queue fallback
     */
    public void sendPasswordResetEmail(String recipientEmail, String otpCode) {
        String subject = "Password Reset Request";
        String content = buildPasswordResetEmailContent(otpCode);
        
        try {
            // Try immediate sending first for password reset (time-sensitive)
            boolean sent = emailProcessorService.sendImmediateEmail(
                recipientEmail, 
                subject, 
                content, 
                EmailQueue.EmailType.PASSWORD_RESET
            );
            
            if (!sent) {
                log.info("Password reset email queued for: {}", recipientEmail);
            }
            
        } catch (Exception e) {
            // Fallback to queue
            emailQueueService.queueEmail(
                recipientEmail, 
                subject, 
                content, 
                EmailQueue.EmailType.PASSWORD_RESET, 
                null
            );
            
            log.info("Password reset email queued due to error for: {}", recipientEmail);
        }
    }

    /**
     * Send admin registration notification
     */
    public void sendAdminRegistrationNotification(String adminUsername, String adminEmail, 
                                                 String adminPhone, Long userId, String superAdminEmail) {
        String subject = "New Admin Registration Request - CineBook";
        String content = buildAdminRegistrationEmailContent(adminUsername, adminEmail, adminPhone, userId);
        
        // Queue this email as it's not time-critical
        emailQueueService.queueEmail(
            superAdminEmail, 
            subject, 
            content, 
            EmailQueue.EmailType.ADMIN_REGISTRATION_NOTIFICATION, 
            userId
        );
        
        log.info("Admin registration notification queued for user: {} (ID: {})", adminUsername, userId);
    }

    /**
     * Send admin approval notification
     */
    public void sendAdminApprovalNotification(String adminEmail, String adminUsername, 
                                            boolean approved, String reason) {
        String subject = approved ? "Admin Access Approved - CineBook" : "Admin Access Denied - CineBook";
        String content = buildAdminApprovalEmailContent(adminUsername, approved, reason);
        
        // Queue this email
        emailQueueService.queueEmail(
            adminEmail, 
            subject, 
            content, 
            EmailQueue.EmailType.ADMIN_APPROVAL_NOTIFICATION, 
            null
        );
        
        log.info("Admin approval notification queued for: {} (approved: {})", adminEmail, approved);
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String recipientEmail, String bookingDetails, Long bookingId) {
        String subject = "🎬 Booking Confirmed - CineBook";
        String content = buildBookingConfirmationEmailContent(bookingDetails);
        
        // Queue booking confirmation
        emailQueueService.queueEmail(
            recipientEmail, 
            subject, 
            content, 
            EmailQueue.EmailType.BOOKING_CONFIRMATION, 
            bookingId
        );
        
        log.info("Booking confirmation email queued for booking ID: {}", bookingId);
    }

    /**
     * Send scheduled email (for future sending)
     */
    public void scheduleEmail(String recipientEmail, String subject, String content, 
                             EmailQueue.EmailType emailType, LocalDateTime scheduledAt, Long relatedEntityId) {
        
        emailQueueService.queueScheduledEmail(
            recipientEmail, 
            subject, 
            content, 
            emailType, 
            relatedEntityId, 
            scheduledAt
        );
        
        log.info("Email scheduled for: {} at {}", recipientEmail, scheduledAt);
    }

    // Email content builders

    private String buildWelcomeEmailContent(User user) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "  <style>"
                + "    body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }"
                + "    .header { background-color: #0d253f; padding: 30px; text-align: center; }"
                + "    .header h1 { color: #fff; margin: 0; font-size: 24px; }"
                + "    .content { padding: 30px; background-color: #f8f9fa; }"
                + "    .features { margin: 20px 0; }"
                + "    .feature-item { display: flex; align-items: flex-start; margin-bottom: 10px; }"
                + "    .feature-icon { margin-right: 10px; font-size: 18px; }"
                + "    .cta-button { display: inline-block; padding: 12px 30px; background-color: #01b4e4; "
                + "                 color: white; text-decoration: none; border-radius: 5px; font-weight: bold; "
                + "                 margin: 20px 0; }"
                + "    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 0.9em; "
                + "             background-color: #e9ecef; }"
                + "    .highlight { color: #01b4e4; font-weight: bold; }"
                + "    .signature { font-family: 'Georgia', serif; font-size: 18px; font-weight: bold; "
                + "                text-align: center; margin: 20px 0; }"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <div class='header'>"
                + "    <h1>CineBook</h1>"
                + "  </div>"
                + "  <div class='content'>"
                + "    <h2>Hi " + user.getUsername() + ",</h2>"
                + "    <p>Welcome to CineBook – where movie magic begins! 🍿</p>"
                + "    <p>We're thrilled to have you onboard.</p>"
                + "    "
                + "    <p>With CineBook, you can:</p>"
                + "    <div class='features'>"
                + "      <div class='feature-item'><span class='feature-icon'>✅</span> Discover the latest movie releases</div>"
                + "      <div class='feature-item'><span class='feature-icon'>✅</span> Book tickets instantly with ease</div>"
                + "      <div class='feature-item'><span class='feature-icon'>✅</span> Enjoy exclusive offers and deals</div>"
                + "      <div class='feature-item'><span class='feature-icon'>✅</span> Choose your favorite seats at top theatres near you</div>"
                + "    </div>"
                + "    "
                + "    <p>🎟 Your entertainment journey starts now.</p>"
                + "    <p>Start booking and experience cinema like never before.</p>"
                + "    "
                + "    <div style='text-align: center;'>"
                + "      <a href='https://yourmoviedekhoapp.com/explore' class='cta-button'>Start Exploring Movies</a>"
                + "    </div>"
                + "    "
                + "    <p>If you have any questions or need help, we're just an email away at "
                + "       <span class='highlight'>tribhuvannathh4567@gmail.com</span>.</p>"
                + "    "
                + "    <div class='signature'>Lights. Camera. Action.</div>"
                + "    "
                + "    <p style='text-align: center; font-weight: bold;'>Team MovieDekho</p>"
                + "  </div>"
                + "  <div class='footer'>"
                + "    <p>© 2025 CineBook. All rights reserved.</p>"
                + "    <p> Kurthaul,Patna   | <a href='https://cinebook.app></a></p>"
                + "    <p>You're receiving this email because you created an account with MovieDekho.</p>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    private String buildOtpEmailContent(String otpCode) {
        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><title>OTP Code</title></head>"
                + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
                + "<h2 style=\"color: #333; text-align: center;\">Your Login OTP is </h2>"
                + "<div style=\"background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 4px; margin: 20px 0;\">"
                + "<h3 style=\"color: #007bff; font-size: 24px; margin: 0;\">" + otpCode + "</h3>"
                + "</div>"
                + "<p style=\"color: #666; text-align: center;\">This code is valid for 5 minutes</p>"
                + "<p style=\"color: #999; font-size: 12px; text-align: center;\">If you didn't request this code, please ignore this email.</p>"
                + "</div>"
                + "</body></html>";
    }

    private String buildPasswordResetEmailContent(String otpCode) {
        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><title>Password Reset</title></head>"
                + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
                + "<h2 style=\"color: #333; text-align: center;\">Your password reset OTP is </h2>"
                + "<div style=\"background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 4px; margin: 20px 0;\">"
                + "<h3 style=\"color: #dc3545; font-size: 24px; margin: 0;\">" + otpCode + "</h3>"
                + "</div>"
                + "<p style=\"color: #666; text-align: center;\">This code is valid for 5 minutes</p>"
                + "<p style=\"color: #999; font-size: 12px; text-align: center;\">If you didn't request this code, please ignore this email.</p>"
                + "</div>"
                + "</body></html>";
    }

    private String buildAdminRegistrationEmailContent(String adminUsername, String adminEmail, 
                                                     String adminPhone, Long userId) {
        String approvalLink = "http://localhost:8080/api/super-admin/approve/" + userId;
        String declineLink = "http://localhost:8080/api/super-admin/decline/" + userId;
        String dateTime = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));

        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><title>Admin Registration</title></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto;\">"
                + "  <div style=\"background: linear-gradient(135deg, #dc3545 0%, #b02a37 100%); padding: 30px; text-align: center;\">"
                + "    <h1 style=\"color: white; margin: 0;\">🎬 New Admin Registration Request</h1>"
                + "  </div>"
                + "  <div style=\"padding: 30px;\">"
                + "    <h2>Admin Details:</h2>"
                + "    <p><strong>Name:</strong> " + adminUsername + "</p>"
                + "    <p><strong>Email:</strong> " + adminEmail + "</p>"
                + "    <p><strong>Phone:</strong> " + adminPhone + "</p>"
                + "    <p><strong>Request Time:</strong> " + dateTime + "</p>"
                + "    <div style=\"text-align: center; margin: 30px 0;\">"
                + "      <a href=\"" + approvalLink + "\" style=\"background-color: #28a745; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 0 10px;\">✅ APPROVE</a>"
                + "      <a href=\"" + declineLink + "\" style=\"background-color: #dc3545; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 0 10px;\">❌ DECLINE</a>"
                + "    </div>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private String buildAdminApprovalEmailContent(String adminUsername, boolean approved, String reason) {
        String statusColor = approved ? "#28a745" : "#dc3545";
        String statusText = approved ? "APPROVED" : "DENIED";
        String statusIcon = approved ? "✅" : "❌";

        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><title>Admin " + statusText + "</title></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto;\">"
                + "  <div style=\"background-color: " + statusColor + "; padding: 30px; text-align: center;\">"
                + "    <h1 style=\"color: white; margin: 0;\">" + statusIcon + " Admin Access " + statusText + "</h1>"
                + "  </div>"
                + "  <div style=\"padding: 30px;\">"
                + "    <h2>Hi " + adminUsername + ",</h2>"
                + "    <p>Your admin access request has been <strong>" + statusText.toLowerCase() + "</strong>.</p>"
                + (reason != null && !reason.trim().isEmpty() ? 
                   "    <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">"
                   + "      <h4>Reason:</h4>"
                   + "      <p>" + reason + "</p>"
                   + "    </div>" : "")
                + "    <p>Thank you for your interest in CineBook.</p>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private String buildBookingConfirmationEmailContent(String bookingDetails) {
        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><title>Booking Confirmed</title></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto;\">"
                + "  <div style=\"background-color: #28a745; padding: 30px; text-align: center;\">"
                + "    <h1 style=\"color: white; margin: 0;\">🎬 Booking Confirmed!</h1>"
                + "  </div>"
                + "  <div style=\"padding: 30px;\">"
                + "    <h2>Your booking has been confirmed!</h2>"
                + "    <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">"
                + "      " + bookingDetails
                + "    </div>"
                + "    <p>Thank you for choosing CineBook!</p>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }
}