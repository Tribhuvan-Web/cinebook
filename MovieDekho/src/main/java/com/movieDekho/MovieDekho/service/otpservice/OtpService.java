package com.movieDekho.MovieDekho.service.otpservice;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_VALIDITY_MINUTES = 5;

    public String generateOtp(String email) {
        String otp = RandomStringUtils.randomNumeric(6);
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_VALIDITY_MINUTES);
        otpStorage.put(email, new OtpData(otp, expirationTime));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData storedData = otpStorage.get(email);
        if (storedData == null) return false;

        if (System.currentTimeMillis() > storedData.expirationTime) {
            otpStorage.remove(email);
            return false;
        }

        if (storedData.otp.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
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