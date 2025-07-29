package com.movieDekho.MovieDekho.controller;

import com.movieDekho.MovieDekho.service.adminService.AdminApprovalService;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

        private final AdminApprovalService adminApprovalService;
        private final BrevoEmailService brevoEmailService;

        @Value("${app.super.admin.email}")
        private String superAdminEmail;

        private final Map<Long, String> otpStorage = new ConcurrentHashMap<>();

        @GetMapping("/approve/{userId}")
        public ResponseEntity<String> showOtpVerification(@PathVariable Long userId) {
                try {
                        String otp = generateOTP();
                        otpStorage.put(userId, otp);
                        sendOtpEmail(otp, userId);

                        String otpForm = "<!DOCTYPE html>" +
                                        "<html>" +
                                        "<head>" +
                                        "    <title>Admin Approval - OTP Verification</title>" +
                                        "    <meta charset=\"UTF-8\">" +
                                        "    <style>" +
                                        "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 50px; }"
                                        +
                                        "        .container { max-width: 500px; margin: 0 auto; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                                        +
                                        "        .header { text-align: center; color: #333; margin-bottom: 30px; }" +
                                        "        .form-group { margin-bottom: 20px; }" +
                                        "        label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }"
                                        +
                                        "        input { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 5px; font-size: 16px; }"
                                        +
                                        "        button { width: 100%; padding: 15px; background-color: #28a745; color: white; border: none; border-radius: 5px; font-size: 16px; cursor: pointer; }"
                                        +
                                        "        button:hover { background-color: #218838; }" +
                                        "        .info { background-color: #d4edda; padding: 15px; border-radius: 5px; margin-bottom: 20px; }"
                                        +
                                        "        .warning { background-color: #fff3cd; padding: 15px; border-radius: 5px; margin-bottom: 20px; }"
                                        +
                                        "    </style>" +
                                        "</head>" +
                                        "<body>" +
                                        "    <div class=\"container\">" +
                                        "        <h1 class=\"header\">🔐 Super Admin Verification</h1>" +
                                        "        " +
                                        "        <div class=\"info\">" +
                                        "            <strong>🛡️ Security Check Required</strong><br>" +
                                        "            An OTP has been sent to your email: <strong>"
                                        + maskEmail(superAdminEmail) + "</strong>" +
                                        "        </div>" +
                                        "        " +
                                        "        <div class=\"warning\">" +
                                        "            <strong>⚠️ Admin Approval Request</strong><br>" +
                                        "            User ID: <strong>" + userId + "</strong><br>" +
                                        "            Action: Approve as Admin" +
                                        "        </div>" +
                                        "        " +
                                        "        <form onsubmit=\"verifyOtp(event)\">" +
                                        "            <div class=\"form-group\">" +
                                        "                <label for=\"otp\">Enter 6-Digit OTP:</label>" +
                                        "                <input type=\"text\" id=\"otp\" name=\"otp\" maxlength=\"6\" pattern=\"[0-9]{6}\" placeholder=\"123456\" required>"
                                        +
                                        "            </div>" +
                                        "            " +
                                        "            <button type=\"submit\">✅ Verify & Approve Admin</button>" +
                                        "        </form>" +
                                        "        " +
                                        "        <div style=\"text-align: center; margin-top: 20px; color: #666;\">" +
                                        "            <small>OTP is valid for 10 minutes</small>" +
                                        "        </div>" +
                                        "    </div>" +
                                        "    " +
                                        "    <script>" +
                                        "        async function verifyOtp(event) {" +
                                        "            event.preventDefault();" +
                                        "            const otp = document.getElementById('otp').value;" +
                                        "            " +
                                        "            if (otp.length !== 6) {" +
                                        "                alert('Please enter a 6-digit OTP');" +
                                        "                return;" +
                                        "            }" +
                                        "            " +
                                        "            try {" +
                                        "                const response = await fetch('/api/super-admin/verify-otp/"
                                        + userId + "', {" +
                                        "                    method: 'POST'," +
                                        "                    headers: {" +
                                        "                        'Content-Type': 'application/json'," +
                                        "                    }," +
                                        "                    body: JSON.stringify({ otp: otp })" +
                                        "                });" +
                                        "                " +
                                        "                const result = await response.text();" +
                                        "                document.body.innerHTML = result;" +
                                        "            } catch (error) {" +
                                        "                alert('Error verifying OTP. Please try again.');" +
                                        "            }" +
                                        "        }" +
                                        "    </script>" +
                                        "</body>" +
                                        "</html>";

                        return ResponseEntity.ok().header("Content-Type", "text/html").body(otpForm);

                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "text/html")
                                        .body(createErrorPage("Failed to generate OTP: " + e.getMessage()));
                }
        }

        @PostMapping("/verify-otp/{userId}")
        public ResponseEntity<String> verifyOtpAndApprove(@PathVariable Long userId,
                        @RequestBody Map<String, String> request) {
                try {
                        String providedOtp = request.get("otp");
                        String storedOtp = otpStorage.get(userId);

                        if (storedOtp == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "text/html")
                                                .body(createErrorPage(
                                                                "OTP expired or invalid. Please try the approval link again."));
                        }

                        if (!storedOtp.equals(providedOtp)) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .header("Content-Type", "text/html")
                                                .body(createErrorPage(
                                                                "Invalid OTP. Please check your email and try again."));
                        }

                        // OTP verified, remove from storage
                        otpStorage.remove(userId);

                        // Approve the admin
                        adminApprovalService.approveAdmin(userId);

                        // Return success page
                        String successHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <title>Admin Approved Successfully</title>
                                            <meta charset="UTF-8">
                                            <style>
                                                body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 50px; }
                                                .container { max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                                                .success { color: #28a745; font-size: 48px; margin-bottom: 20px; }
                                                .title { color: #333; font-size: 24px; margin-bottom: 15px; }
                                                .details { color: #666; font-size: 16px; line-height: 1.6; }
                                                .info-box { background-color: #d4edda; padding: 20px; border-radius: 5px; margin: 20px 0; }
                                            </style>
                                        </head>
                                        <body>
                                            <div class="container">
                                                <div class="success">✅</div>
                                                <h1 class="title">Admin Approved Successfully!</h1>

                                                <div class="info-box">
                                                    <strong>🎉 Approval Complete</strong><br>
                                                    User ID: <strong>"""
                                        + userId + """
                                                        </strong>
                                                        <br>
                                                        Status: <strong>APPROVED</strong><br>
                                                        Timestamp: <strong>""" + new java.util.Date() + """
                                                                    </strong>
                                                                </div>

                                                                <div class="details">
                                                                    ✓ The admin has been successfully approved<br>
                                                                    ✓ Admin notification email has been sent<br>
                                                                    ✓ The user now has admin access to the system
                                                                </div>

                                                                <div style="margin-top: 30px; color: #888;">
                                                                    You can close this window now.
                                                                </div>
                                                            </div>
                                                        </body>
                                                        </html>
                                                        """;

                        return ResponseEntity.ok()
                                        .header("Content-Type", "text/html")
                                        .body(successHtml);

                } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .header("Content-Type", "text/html")
                                        .body(createErrorPage("Approval failed: " + e.getMessage()));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("Content-Type", "text/html")
                                        .body(createErrorPage("System error: " + e.getMessage()));
                }
        }

        private String generateOTP() {
                Random random = new Random();
                int otp = 100000 + random.nextInt(900000); // 6-digit OTP
                return String.valueOf(otp);
        }

        private void sendOtpEmail(String otp, Long userId) {
                try {
                        String subject = "🔐 Admin Approval OTP - MovieDekho";
                        String htmlContent = """
                                        <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>
                                            <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; color: white;'>
                                                <h1 style='margin: 0; font-size: 28px;'>🔐 Super Admin OTP</h1>
                                                <p style='margin: 10px 0 0 0; opacity: 0.9;'>Admin Approval Verification Required</p>
                                            </div>

                                            <div style='padding: 30px; background-color: #f8f9fa;'>
                                                <div style='background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>
                                                    <h2 style='color: #333; margin-top: 0;'>🚨 Admin Approval Request</h2>

                                                    <div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;'>
                                                        <strong>⚠️ Security Alert:</strong><br>
                                                        Someone is requesting to approve User ID: <strong>"""
                                        + userId
                                        + """
                                                            </strong> as an admin.
                                                        </div>

                                                        <div style='text-align: center; margin: 30px 0;'>
                                                            <div style='background-color: #e7f3ff; padding: 20px; border-radius: 8px; display: inline-block;'>
                                                                <p style='margin: 0 0 10px 0; color: #666;'>Your OTP Code:</p>
                                                                <div style='font-size: 36px; font-weight: bold; color: #007bff; letter-spacing: 8px;'>"""
                                        + otp
                                        + """
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div style='background-color: #d4edda; padding: 15px; border-radius: 5px; margin: 20px 0;'>
                                                            <strong>🛡️ Security Instructions:</strong><br>
                                                            • Enter this OTP in the verification form<br>
                                                            • OTP is valid for 10 minutes only<br>
                                                            • Never share this OTP with anyone<br>
                                                            • If you didn't request this, ignore this email
                                                        </div>

                                                        <div style='text-align: center; margin-top: 30px;'>
                                                            <p style='color: #666; font-size: 14px;'>
                                                                This is an automated security email from MovieDekho Admin System.<br>
                                                                Generated at: """
                                        + new java.util.Date() + """
                                                                        </p>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        """;

                        brevoEmailService.sendEmail(superAdminEmail, subject, htmlContent);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
                }
        }

        private String maskEmail(String email) {
                if (email == null || !email.contains("@")) {
                        return "***@***.com";
                }
                String[] parts = email.split("@");
                String username = parts[0];
                String domain = parts[1];

                if (username.length() <= 2) {
                        return "*".repeat(username.length()) + "@" + domain;
                }

                return username.substring(0, 2) + "*".repeat(username.length() - 2) + "@" + domain;
        }

        private String createErrorPage(String errorMessage) {
                return """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <title>Error - MovieDekho</title>
                                    <meta charset="UTF-8">
                                    <style>
                                        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 50px; }
                                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
                                        .error { color: #dc3545; font-size: 48px; margin-bottom: 20px; }
                                        .title { color: #333; font-size: 24px; margin-bottom: 15px; }
                                        .details { color: #666; font-size: 16px; }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="error">❌</div>
                                        <h1 class="title">Verification Failed</h1>
                                        <div class="details">"""
                                + errorMessage
                                + """
                                                        </div>
                                                        <div style="margin-top: 30px; color: #888;">
                                                            Please contact the system administrator if this error persists.
                                                        </div>
                                                    </div>
                                                </body>
                                                </html>
                                                """;
        }
}
