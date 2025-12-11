package za.ac.styling.util;

import java.util.regex.Pattern;

/**
 * Generic helper class for data validation used across all domain classes
 */
public class ValidationHelper {

    // Regular expression patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s'-]{2,50}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(
            "^[0-9]{4,10}$"
    );

    private static final Pattern SKU_PATTERN = Pattern.compile(
            "^[A-Z0-9-]{5,20}$"
    );

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(
            "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
    );

    // ==================== STRING VALIDATIONS ====================

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates username format
     * Username must be 3-20 characters, alphanumeric and underscores only
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validates password strength
     * Password must be at least 8 characters
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 8;
    }

    /**
     * Validates password with strong requirements
     * Must contain at least 8 characters, one uppercase, one lowercase, one digit, and one special character
     */
    public static boolean isValidStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Validates name format (first name or last name)
     * Name must be 2-50 characters, letters, spaces, hyphens, and apostrophes only
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates phone number format
     * Accepts various international formats
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates SKU format for products
     */
    public static boolean isValidSKU(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        return SKU_PATTERN.matcher(sku.trim()).matches();
    }

    /**
     * Validates hex color code format
     */
    public static boolean isValidHexColor(String hexCode) {
        if (hexCode == null || hexCode.trim().isEmpty()) {
            return false;
        }
        return HEX_COLOR_PATTERN.matcher(hexCode.trim()).matches();
    }

    /**
     * Validates zip/postal code
     */
    public static boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        return ZIP_CODE_PATTERN.matcher(zipCode.trim()).matches();
    }

    /**
     * Checks if a string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates string length is within range
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    // ==================== NUMERIC VALIDATIONS ====================

    /**
     * Validates that a price is positive
     */
    public static boolean isValidPrice(double price) {
        return price >= 0;
    }

    /**
     * Validates that a quantity is positive
     */
    public static boolean isValidQuantity(int quantity) {
        return quantity >= 0;
    }

    /**
     * Validates that a rating is within range (1-5)
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /**
     * Validates that a percentage is within range (0-100)
     */
    public static boolean isValidPercentage(double percentage) {
        return percentage >= 0 && percentage <= 100;
    }

    /**
     * Validates that a weight is positive
     */
    public static boolean isValidWeight(double weight) {
        return weight > 0;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Sanitizes input by trimming whitespace
     */
    public static String sanitizeInput(String input) {
        return input == null ? null : input.trim();
    }

    /**
     * Formats a full name from first and last name
     */
    public static String formatFullName(String firstName, String lastName) {
        if (isNullOrEmpty(firstName) && isNullOrEmpty(lastName)) {
            return "";
        }

        String first = sanitizeInput(firstName);
        String last = sanitizeInput(lastName);

        if (isNullOrEmpty(first)) return last;
        if (isNullOrEmpty(last)) return first;

        return first + " " + last;
    }

    /**
     * Validates card number using Luhn algorithm
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        // Check if only digits
        if (!cardNumber.matches("\\d+")) {
            return false;
        }

        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Validates CVV format (3-4 digits)
     */
    public static boolean isValidCVV(String cvv) {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        return cvv.matches("^[0-9]{3,4}$");
    }

    /**
     * Validates expiry date format (MM/YY or MM/YYYY)
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        return expiryDate.matches("^(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$");
    }

    /**
     * Validates URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            new java.net.URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== VALIDATION RESULT CLASS ====================

    /**
     * Inner class to hold validation results
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public java.util.List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }

        public String getFormattedErrors() {
            if (errors.isEmpty()) {
                return "No errors";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < errors.size(); i++) {
                sb.append((i + 1)).append(". ").append(errors.get(i));
                if (i < errors.size() - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }
}