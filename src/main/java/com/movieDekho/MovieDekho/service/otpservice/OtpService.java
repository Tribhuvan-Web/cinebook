package com.movieDekho.MovieDekho.service.otpservice;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_VALIDITY_MINUTES = 5;
    
    // OTP type prefixes to avoid conflicts
    private static final String LOGIN_OTP_PREFIX = "LOGIN:";
    private static final String PASSWORD_RESET_OTP_PREFIX = "RESET:";

    public String generateOtp(String email) {
        return generateOtpForType(email, LOGIN_OTP_PREFIX);
    }
    
    public String generateLoginOtp(String email) {
        return generateOtpForType(email, LOGIN_OTP_PREFIX);
    }
    
    public String generatePasswordResetOtp(String email) {
        return generateOtpForType(email, PASSWORD_RESET_OTP_PREFIX);
    }
    
    private String generateOtpForType(String email, String type) {
        String otp = RandomStringUtils.randomNumeric(6);
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_VALIDITY_MINUTES);
        String key = type + email;
        otpStorage.put(key, new OtpData(otp, expirationTime));
        logger.info("Generated {} OTP for email: {} with key: {} and OTP: {}", 
                   type.replace(":", ""), email, key, otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        return validateOtpForType(email, otp, LOGIN_OTP_PREFIX);
    }
    
    public boolean validateLoginOtp(String email, String otp) {
        return validateOtpForType(email, otp, LOGIN_OTP_PREFIX);
    }
    
    public boolean validatePasswordResetOtp(String email, String otp) {
        return validateOtpForType(email, otp, PASSWORD_RESET_OTP_PREFIX);
    }
    
    private boolean validateOtpForType(String email, String otp, String type) {
        String key = type + email;
        logger.info("Validating {} OTP for email: {} with key: {} and provided OTP: {}", 
                   type.replace(":", ""), email, key, otp);
        
        OtpData storedData = otpStorage.get(key);
        if (storedData == null) {
            logger.warn("No OTP found for key: {}", key);
            return false;
        }

        if (System.currentTimeMillis() > storedData.expirationTime) {
            logger.warn("OTP expired for key: {}", key);
            otpStorage.remove(key);
            return false;
        }

        if (storedData.otp.equals(otp)) {
            logger.info("OTP validation successful for key: {}", key);
            otpStorage.remove(key);
            return true;
        }
        
        logger.warn("OTP mismatch for key: {}. Expected: {}, Provided: {}", key, storedData.otp, otp);
        return false;
    }
    
    private static class OtpData {
        String otp;
        long expirationTime;

        OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }
}