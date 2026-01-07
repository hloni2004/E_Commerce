
package za.ac.styling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.PasswordResetToken;
import za.ac.styling.domain.User;
import za.ac.styling.repository.PasswordResetTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private static final int TOKEN_LENGTH = 32;
    private static final int EXPIRATION_HOURS = 1;
    private static final int OTP_LENGTH = 6;

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public za.ac.styling.service.PasswordResetResult createPasswordResetToken(String email, String frontendBaseUrl) {

        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Optional<User> userOptional = userService.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {

            return new za.ac.styling.service.PasswordResetResult(null, false);
        }

        User user = userOptional.get();

        tokenRepository.deleteByUser(user);

        String token = generateToken();
        String otpCode = generateOTP();

        LocalDateTime expiryDate = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(EXPIRATION_HOURS);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .otpCode(otpCode)
                .otpVerified(false)
                .emailSent(false)
                .lastSentAt(null)
                .createdAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC))
                .build();

        tokenRepository.save(resetToken);

        String encodedToken = java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        String resetLink = frontendBaseUrl + "/auth/reset-password?token=" + encodedToken;
        String userName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        boolean emailSent = false;
        String errorMessage = null;
        try {
            emailService.sendPasswordResetEmailWithOTP(user.getEmail(), resetLink, userName, otpCode);
            logger.info("Password reset email dispatched to: {}", user.getEmail());
            emailSent = true;
            resetToken.setEmailSent(true);
            resetToken.setLastSentAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
            tokenRepository.save(resetToken);
        } catch (Exception e) {

            logger.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
            errorMessage = e.getMessage();
            resetToken.setEmailSent(false);
            resetToken.setLastSentAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
            tokenRepository.save(resetToken);

        }

        return new za.ac.styling.service.PasswordResetResult(token, emailSent, errorMessage);
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty()) {
            return false;
        }

        PasswordResetToken passwordResetToken = resetToken.get();

        return !passwordResetToken.isUsed() && !passwordResetToken.isExpired();
    }

    @Transactional
    public boolean verifyOTP(String token, String otpCode) {
        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(token);

        if (resetTokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        if (resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        if (resetToken.getOtpCode() != null && resetToken.getOtpCode().equals(otpCode)) {
            resetToken.setOtpVerified(true);
            tokenRepository.save(resetToken);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(token);

        if (resetTokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        if (resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        if (!resetToken.isOtpVerified()) {
            return false;
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must not be the same as the previous password");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userService.update(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
