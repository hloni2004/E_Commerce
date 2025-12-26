package za.ac.styling.service;

/**
 * Simple DTO for password reset creation result.
 */
public record PasswordResetResult(String token, boolean emailSent) {
}
