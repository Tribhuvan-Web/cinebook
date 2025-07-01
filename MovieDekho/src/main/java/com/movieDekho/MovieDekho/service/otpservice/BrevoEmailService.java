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

    // Add verified sender email from your Brevo account
    @Value("${brevo.sender.email:noreply@yourdomain.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:Your App Name}")
    private String senderName;

    public void sendOtpEmail(String recipientEmail, String otpCode) {
        logger.info("Attempting to send OTP email to: {}", recipientEmail);

        // Validate inputs
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("Brevo API key is not configured");
            throw new RuntimeException("Brevo API key is not configured");
        }

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            logger.error("Recipient email is empty");
            throw new RuntimeException("Recipient email is required");
        }

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // Set the correct Brevo API endpoint
            defaultClient.setBasePath("https://api.brevo.com/v3");

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            // CRITICAL: Use a verified sender email from your Brevo account
            SendSmtpEmailSender sender = new SendSmtpEmailSender()
                    .email(senderEmail)  // This MUST be verified in your Brevo account
                    .name(senderName);

            SendSmtpEmailTo to = new SendSmtpEmailTo()
                    .email(recipientEmail.trim());

            // Improved HTML content with better formatting
            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head><meta charset=\"UTF-8\"><title>OTP Code</title></head>"
                    + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
                    + "<h2 style=\"color: #333; text-align: center;\">Your Login OTP</h2>"
                    + "<div style=\"background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 4px; margin: 20px 0;\">"
                    + "<h3 style=\"color: #007bff; font-size: 24px; margin: 0;\">" + otpCode + "</h3>"
                    + "</div>"
                    + "<p style=\"color: #666; text-align: center;\">This code is valid for 5 minutes</p>"
                    + "<p style=\"color: #999; font-size: 12px; text-align: center;\">If you didn't request this code, please ignore this email.</p>"
                    + "</div>"
                    + "</body></html>";

            // Also provide plain text version for better deliverability
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
            logger.info("Email sent successfully! Message ID: {}", response.getMessageId());
            logger.debug("Brevo API response: {}", response.toString());

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
}