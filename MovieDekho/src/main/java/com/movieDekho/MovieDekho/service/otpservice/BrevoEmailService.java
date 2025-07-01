package com.movieDekho.MovieDekho.service.otpservice;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class BrevoEmailService {
    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email:noreply@yourdomain.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:Your App Name}")
    private String senderName;

    public void sendOtpEmail(String recipientEmail, String otpCode) {

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            defaultClient.setBasePath("https://api.brevo.com/v3");

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName);

            SendSmtpEmailTo to = new SendSmtpEmailTo()
                    .email(recipientEmail.trim());

            String htmlContent = "<!DOCTYPE html>"
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

            String textContent = "Your Login OTP: " + otpCode + "\n\n"
                    + "This code is valid for 5 minutes.\n"
                    + "If you didn't request this code, please ignore this email.";

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail()
                    .sender(sender)
                    .to(Collections.singletonList(to))
                    .subject("Your Login OTP Code")
                    .htmlContent(htmlContent)
                    .textContent(textContent)  // Add plain text version
                    .tags(Collections.singletonList("otp-email"));  // Add tags for tracking

            CreateSmtpEmail response = apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", recipientEmail, e.getMessage(), e);

            // Log more specific error information
            if (e.getMessage() != null) {
                if (e.getMessage().contains("401")) {
                    logger.error("Authentication failed - check your API key");
                } else if (e.getMessage().contains("400")) {
                    logger.error("Bad request - check sender email verification and request parameters");
                } else if (e.getMessage().contains("402")) {
                    logger.error("Account limit exceeded or payment required");
                }
            }
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String recipientEmail, String otpCode) {
        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            defaultClient.setBasePath("https://api.brevo.com/v3");

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender()
                    .email(senderEmail)  // This MUST be verified in your Brevo account
                    .name(senderName);

            SendSmtpEmailTo to = new SendSmtpEmailTo()
                    .email(recipientEmail.trim());

            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head><meta charset=\"UTF-8\"><title>Password Reset</title></head>"
                    + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
                    + "<h2 style=\"color: #333; text-align: center;\">Password Reset Request</h2>"
                    + "<div style=\"background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 4px; margin: 20px 0;\">"
                    + "<h3 style=\"color: #dc3545; font-size: 24px; margin: 0;\">" + otpCode + "</h3>"
                    + "</div>"
                    + "<p style=\"color: #666; text-align: center;\">This code is valid for 5 minutes</p>"
                    + "<p style=\"color: #999; font-size: 12px; text-align: center;\">If you didn't request this code, please ignore this email.</p>"
                    + "</div>"
                    + "</body></html>";

            String textContent = "Password Reset Request\n\n"
                    + "Your password reset OTP: " + otpCode + "\n\n"
                    + "This code is valid for 5 minutes.\n"
                    + "If you didn't request this code, please ignore this email.";

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail()
                    .sender(sender)
                    .to(Collections.singletonList(to))
                    .subject("Password Reset Request")
                    .htmlContent(htmlContent)
                    .textContent(textContent)  // Add plain text version
                    .tags(Collections.singletonList("password-reset-email"));  // Add tags for tracking

            CreateSmtpEmail response = apiInstance.sendTransacEmail(sendSmtpEmail);
            logger.info("Password reset email sent successfully! Message ID: {}", response.getMessageId());

        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    public void sendEmail(String recipientEmail, String subject, String htmlContent) {

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);
        defaultClient.setBasePath("https://api.brevo.com/v3");

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender()
                .email(senderEmail)
                .name(senderName);

        SendSmtpEmailTo to = new SendSmtpEmailTo()
                .email(recipientEmail);

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail()
                .sender(sender)
                .to(Collections.singletonList(to))
                .subject(subject)
                .htmlContent(htmlContent);

        try {
            CreateSmtpEmail response = apiInstance.sendTransacEmail(sendSmtpEmail);
            logger.info("Email sent successfully! Message ID: {}", response.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to send email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}