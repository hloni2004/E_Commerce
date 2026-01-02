package za.ac.styling.service;

/**
 * Simple DTO for password reset creation result.
 * Added optional errorMessage to carry debug information when enabled.
 */
public record PasswordResetResult(String token, boolean emailSent, String errorMessage) {
    public PasswordResetResult(String token, boolean emailSent) {
        this(token, emailSent, null);
    }
}
