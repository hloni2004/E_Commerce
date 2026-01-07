package za.ac.styling.factory;

import za.ac.styling.domain.PayPalPayment;
import za.ac.styling.domain.User;
import za.ac.styling.util.ValidationHelper;

public class PayPalPaymentFactory {

    public static PayPalPayment createPayPalPayment(String email, String payerId, User user) {

        if (!ValidationHelper.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (ValidationHelper.isNullOrEmpty(payerId)) {
            throw new IllegalArgumentException("Payer ID cannot be empty");
        }

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return PayPalPayment.builder()
                .email(email.toLowerCase().trim())
                .payerId(payerId.trim())
                .user(user)
                .build();
    }

    public static PayPalPayment createPayPalPayment(String email, User user) {
        if (!ValidationHelper.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return PayPalPayment.builder()
                .email(email.toLowerCase().trim())
                .payerId("")
                .user(user)
                .build();
    }

    public static boolean validatePayPalPayment(PayPalPayment payment) {
        if (payment == null) {
            return false;
        }
        return payment.validate();
    }

    public static PayPalPayment updatePayerId(PayPalPayment payment, String payerId) {
        if (payment == null) {
            throw new IllegalArgumentException("PayPalPayment cannot be null");
        }

        if (ValidationHelper.isNullOrEmpty(payerId)) {
            throw new IllegalArgumentException("Payer ID cannot be empty");
        }

        payment.setPayerId(payerId.trim());
        return payment;
    }

    public static PayPalPayment updateEmail(PayPalPayment payment, String email) {
        if (payment == null) {
            throw new IllegalArgumentException("PayPalPayment cannot be null");
        }

        if (!ValidationHelper.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        payment.setEmail(email.toLowerCase().trim());
        return payment;
    }

    public static String maskEmail(String email) {
        if (ValidationHelper.isNullOrEmpty(email) || !email.contains("@")) {
            return "****@****.com";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() > 2 ? 
                localPart.substring(0, 2) + "****" : "****";

        return maskedLocal + "@" + domain;
    }
}
