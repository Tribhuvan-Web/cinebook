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

import java.time.format.DateTimeFormatter;
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

    @Value("${app.super.admin.email}")
    private String superAdminEmail;

    @Value("${app.super.admin.name}")
    private String superAdminName;

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
                    .textContent(textContent) // Add plain text version
                    .tags(Collections.singletonList("otp-email")); // Add tags for tracking

            apiInstance.sendTransacEmail(sendSmtpEmail);
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
                    .email(senderEmail) // This MUST be verified in your Brevo account
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
                    .textContent(textContent) // Add plain text version
                    .tags(Collections.singletonList("password-reset-email")); // Add tags for tracking

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

    public void sendAdminRegistrationNotification(String adminUsername, String adminEmail, String adminPhone, Long userId) {
        try {
            String subject = "New Admin Registration Request - MovieDekho";
            String approvalLink = "http://localhost:8080/api/super-admin/approve/" + userId;
            String dateTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));

            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "<title>Admin Registration Request</title>"
                    + "<style>"
                    + "  @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');"
                    + "  body { font-family: 'Poppins', Arial, sans-serif; margin: 0; padding: 0; background-color: #f9fafb; }"
                    + "  .container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }"
                    + "  .header { background: linear-gradient(135deg, #dc3545 0%, #b02a37 100%); padding: 30px; text-align: center; }"
                    + "  .logo { color: white; font-size: 28px; font-weight: 700; letter-spacing: 0.5px; display: flex; align-items: center; justify-content: center; }"
                    + "  .logo-icon { margin-right: 12px; font-size: 32px; }"
                    + "  .content { padding: 30px; }"
                    + "  .card { background-color: #f8f9fa; border-radius: 10px; padding: 25px; margin-bottom: 25px; }"
                    + "  .card-title { color: #2d3748; font-size: 18px; font-weight: 600; margin-top: 0; margin-bottom: 20px; }"
                    + "  .detail-row { display: flex; margin-bottom: 12px; }"
                    + "  .detail-label { flex: 0 0 140px; color: #4a5568; font-weight: 500; }"
                    + "  .detail-value { flex: 1; color: #1a202c; font-weight: 400; }"
                    + "  .approval-section { background-color: #e6f7ff; border-left: 4px solid #1890ff; padding: 25px; text-align: center; margin: 30px 0; border-radius: 0 10px 10px 0; }"
                    + "  .approval-title { color: #004085; font-size: 20px; margin-top: 0; margin-bottom: 20px; }"
                    + "  .btn-approve { display: inline-block; padding: 14px 35px; background: linear-gradient(to right, #28a745, #218838); color: white !important; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(40,167,69,0.3); transition: all 0.3s; }"
                    + "  .btn-approve:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(40,167,69,0.4); }"
                    + "  .security-note { background-color: #fff8f6; border-left: 4px solid #ff4d4f; padding: 20px; border-radius: 0 8px 8px 0; margin: 30px 0; }"
                    + "  .security-title { color: #721c24; font-size: 18px; margin-top: 0; margin-bottom: 10px; }"
                    + "  .footer { text-align: center; padding: 20px; color: #718096; font-size: 12px; border-top: 1px solid #e2e8f0; }"
                    + "  .link-code { background: #edf2f7; padding: 10px 15px; border-radius: 6px; font-size: 13px; word-break: break-all; margin: 20px 0; display: inline-block; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class=\"container\">"
                    + "  <div class=\"header\">"
                    + "    <div class=\"logo\"><span class=\"logo-icon\">🎬</span> MovieDekho Admin Portal</div>"
                    + "  </div>"
                    + "  <div class=\"content\">"
                    + "    <div class=\"card\">"
                    + "      <h2 class=\"card-title\">New Admin Registration Request</h2>"
                    + "      <p>A new user has requested admin privileges and requires your approval.</p>"
                    + "    </div>"
                    + "    <div>"
                    + "      <h3 class=\"card-title\">Registration Details</h3>"
                    + "      <div class=\"detail-row\"><div class=\"detail-label\">User ID:</div><div class=\"detail-value\">" + userId + "</div></div>"
                    + "      <div class=\"detail-row\"><div class=\"detail-label\">Username:</div><div class=\"detail-value\">" + adminUsername + "</div></div>"
                    + "      <div class=\"detail-row\"><div class=\"detail-label\">Email:</div><div class=\"detail-value\">" + adminEmail + "</div></div>"
                    + "      <div class=\"detail-row\"><div class=\"detail-label\">Phone:</div><div class=\"detail-value\">" + adminPhone + "</div></div>"
                    + "      <div class=\"detail-row\"><div class=\"detail-label\">Request Time:</div><div class=\"detail-value\">" + dateTime + "</div></div>"
                    + "    </div>"
                    + "    <div class=\"approval-section\">"
                    + "      <h3 class=\"approval-title\">Approve Admin Access</h3>"
                    + "      <a href=\"" + approvalLink + "\" class=\"btn-approve\">APPROVE ADMIN</a>"
                    + "      <div style=\"margin-top: 25px;\">"
                    + "        <p>Or manually copy this approval link:</p>"
                    + "        <div class=\"link-code\">" + approvalLink + "</div>"
                    + "      </div>"
                    + "    </div>"
                    + "    <div class=\"security-note\">"
                    + "      <h3 class=\"security-title\">Security Notice</h3>"
                    + "      <p>This user <strong>will not</strong> have administrative privileges until you approve this request. Only approve requests from trusted individuals.</p>"
                    + "      <p>If you don't recognize this request, please investigate immediately.</p>"
                    + "    </div>"
                    + "  </div>"
                    + "  <div class=\"footer\">"
                    + "    <p>This is an automated security notification from MovieDekho Admin System</p>"
                    + "    <p>© " + java.time.Year.now().getValue() + " MovieDekho. All rights reserved.</p>"
                    + "  </div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            sendEmail(superAdminEmail, subject, htmlContent);
            logger.info("Admin registration notification with approval link sent to super admin for user: {}", adminUsername);

        } catch (Exception e) {
            logger.error("Failed to send admin registration notification for user: {}", adminUsername, e);
            // Don't throw exception to avoid failing the registration process
        }
    }

    public void sendAdminApprovalNotification(String adminEmail, String adminUsername, boolean approved, String reason) {
        try {
            String subject = approved ? "Admin Access Approved - MovieDekho" : "Admin Access Denied - MovieDekho";
            String status = approved ? "APPROVED" : "REJECTED";
            String statusColor = approved ? "#28a745" : "#dc3545";
            String statusIcon = approved ? "✅" : "❌";
            String headerBg = approved ?
                    "background: linear-gradient(135deg, #28a745 0%, #218838 100%);" :
                    "background: linear-gradient(135deg, #dc3545 0%, #b02a37 100%);";

            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "<title>Admin Access " + status + "</title>"
                    + "<style>"
                    + "  @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');"
                    + "  body { font-family: 'Poppins', Arial, sans-serif; margin: 0; padding: 0; background-color: #f9fafb; }"
                    + "  .container { max-width: 650px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }"
                    + "  .header { " + headerBg + " padding: 35px; text-align: center; }"
                    + "  .status { color: white; font-size: 32px; font-weight: 700; letter-spacing: 0.5px; display: flex; align-items: center; justify-content: center; }"
                    + "  .status-icon { margin-right: 15px; font-size: 40px; }"
                    + "  .content { padding: 35px; }"
                    + "  .card { background-color: " + (approved ? "#e8f5e9" : "#ffebee") + "; border-left: 4px solid " + statusColor + "; padding: 30px; border-radius: 0 10px 10px 0; margin-bottom: 30px; }"
                    + "  .card-title { color: " + (approved ? "#1b5e20" : "#b71c1c") + "; font-size: 22px; font-weight: 600; margin-top: 0; margin-bottom: 15px; }"
                    + "  .message { color: " + (approved ? "#1b5e20" : "#b71c1c") + "; font-size: 16px; line-height: 1.6; margin: 0; }"
                    + "  .details { background-color: #f8f9fa; padding: 30px; border-radius: 10px; margin: 30px 0; }"
                    + "  .details-title { color: #2d3748; font-size: 20px; font-weight: 600; margin-top: 0; margin-bottom: 20px; }"
                    + "  .detail-row { display: flex; margin-bottom: 15px; }"
                    + "  .detail-label { flex: 0 0 120px; color: #4a5568; font-weight: 500; }"
                    + "  .detail-value { flex: 1; color: #1a202c; font-weight: 400; }"
                    + "  .footer { text-align: center; padding: 25px; color: #718096; font-size: 13px; border-top: 1px solid #e2e8f0; }"
                    + "  .list { padding-left: 20px; margin: 20px 0; }"
                    + "  .list li { margin-bottom: 12px; color: #2d3748; }"
                    + "  .reason-box { background-color: #fff3cd; padding: 20px; border-radius: 8px; margin-top: 20px; }"
                    + "  .reason-title { color: #856404; font-weight: 600; margin-top: 0; }"
                    + "  .reason-text { color: #856404; margin: 0; line-height: 1.6; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class=\"container\">"
                    + "  <div class=\"header\">"
                    + "    <div class=\"status\"><span class=\"status-icon\">" + statusIcon + "</span> Admin Access " + status + "</div>"
                    + "  </div>"
                    + "  <div class=\"content\">"
                    + "    <div class=\"card\">"
                    + "      <h2 class=\"card-title\">Hello " + adminUsername + ",</h2>"
                    + "      <p class=\"message\">Your admin registration request has been <strong>" + status.toLowerCase() + "</strong>.</p>";

            if (approved) {
                htmlContent += "</div>"
                        + "<div class=\"details\">"
                        + "  <h3 class=\"details-title\">Welcome to MovieDekho Admin Panel!</h3>"
                        + "  <p>You now have access to these administrative features:</p>"
                        + "  <ul class=\"list\">"
                        + "    <li>Manage movies and screening slots</li>"
                        + "    <li>View and analyze dashboard metrics</li>"
                        + "    <li>Manage user accounts and permissions</li>"
                        + "    <li>Configure system settings</li>"
                        + "    <li>Access all admin endpoints</li>"
                        + "  </ul>"
                        + "  <p style=\"margin-top: 20px;\">Please use your admin privileges responsibly and maintain security best practices.</p>"
                        + "</div>";
            } else {
                htmlContent += "  <p class=\"message\">You will not be granted administrative privileges at this time.</p>"
                        + "</div>"
                        + "<div class=\"details\">"
                        + "  <h3 class=\"details-title\">Request Details</h3>"
                        + "  <div class=\"detail-row\"><div class=\"detail-label\">Status:</div><div class=\"detail-value\"><strong style=\"color: #dc3545;\">Rejected</strong></div></div>"
                        + "  <div class=\"reason-box\">"
                        + "    <h4 class=\"reason-title\">Reason for Rejection:</h4>"
                        + "    <p class=\"reason-text\">" + (reason != null ? reason : "No specific reason was provided.") + "</p>"
                        + "  </div>"
                        + "  <p style=\"margin-top: 20px;\">If you believe this is an error or would like to appeal this decision, please contact our support team.</p>"
                        + "</div>";
            }

            htmlContent += "  </div>"
                    + "  <div class=\"footer\">"
                    + "    <p>This is an automated notification from MovieDekho Admin System</p>"
                    + "    <p>© " + java.time.Year.now().getValue() + " MovieDekho. All rights reserved.</p>"
                    + "  </div>"
                    + "</div>"
                    + "</body></html>";

            sendEmail(adminEmail, subject, htmlContent);
            logger.info("Admin approval notification sent to: {}", adminEmail);

        } catch (Exception e) {
            logger.error("Failed to send admin approval notification to: {}", adminEmail, e);
        }
    }
}