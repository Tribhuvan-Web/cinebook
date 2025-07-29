package com.movieDekho.MovieDekho.service.adminService;

import com.movieDekho.MovieDekho.models.User;
import com.movieDekho.MovieDekho.repository.UserRepository;
import com.movieDekho.MovieDekho.service.otpservice.BrevoEmailService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(AdminApprovalService.class);

    private final UserRepository userRepository;
    private final BrevoEmailService emailService;

    public String approveAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOpt.get();

        if (!"PENDING_ADMIN".equals(user.getRole())) {
            return "User is not in pending admin state or already processed";
        }

        // Approve the admin
        user.setRole("ROLE_ADMIN");
        user.setIsApproved(true);
        user.setApprovedBy("Super Admin");
        user.setApprovedAt(LocalDateTime.now());

        userRepository.save(user);

        // Send approval notification to the admin
        emailService.sendAdminApprovalNotification(
                user.getEmail(),
                user.getUsername(),
                true,
                null);

        logger.info("Admin approved: {} by Super Admin", user.getUsername());
        return "Admin approved successfully! " + user.getUsername() + " has been notified.";
    }
}
