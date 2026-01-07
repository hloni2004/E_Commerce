package za.ac.styling.service;

public record PasswordResetResult(String token, boolean emailSent, String errorMessage) {
    public PasswordResetResult(String token, boolean emailSent) {
        this(token, emailSent, null);
    }
}
