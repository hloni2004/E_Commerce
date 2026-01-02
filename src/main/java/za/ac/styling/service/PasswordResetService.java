
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

    /**
     * Generate a secure random token
     */
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generate a random 6-digit OTP code
     */
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Generates number between 100000 and 999999
        return String.valueOf(otp);
    }

    /**
     * Create and send password reset token
     * Returns a result containing the token and whether email was dispatched
     */
    @Transactional
    public za.ac.styling.service.PasswordResetResult createPasswordResetToken(String email, String frontendBaseUrl) {
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Don't reveal whether email exists for security
            return new za.ac.styling.service.PasswordResetResult(null, false);
        }

        User user = userOptional.get();

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Create new token and OTP
        String token = generateToken();
        String otpCode = generateOTP();
        // Use UTC for all token timestamps to avoid timezone mismatch
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

        // Send email - don't let mail failures break the flow; log them and return
        // token so clients get 200. Ensure token in URL is URL-encoded.
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
            // Log and continue. Token is still created in DB so admin/ops can inspect and
            // resend.
            logger.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
            errorMessage = e.getMessage();
            resetToken.setEmailSent(false);
            resetToken.setLastSentAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
            tokenRepository.save(resetToken);
            // Do not rethrow: we want the endpoint to return 200 with generic message
        }

        // Return token for development purposes along with email dispatch status
        return new za.ac.styling.service.PasswordResetResult(token, emailSent, errorMessage);
    }

    /**
     * Validate reset token
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty()) {
            return false;
        }

        PasswordResetToken passwordResetToken = resetToken.get();

        return !passwordResetToken.isUsed() && !passwordResetToken.isExpired();
    }

    /**
     * Verify OTP code for token
     */
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

        // Check if OTP matches
        if (resetToken.getOtpCode() != null && resetToken.getOtpCode().equals(otpCode)) {
            resetToken.setOtpVerified(true);
            tokenRepository.save(resetToken);
            return true;
        }

        return false;
    }

    /**
     * Reset password using token (requires OTP verification first)
     */
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

        // Check if OTP has been verified
        if (!resetToken.isOtpVerified()) {
            return false;
        }

        // Update user password (hash with BCrypt)
        User user = resetToken.getUser();
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userService.update(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }

    /**
     * Clean up expired tokens (should be run periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
