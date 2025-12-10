package za.ac.styling.factory;

import za.ac.styling.domain.CardPayment;
import za.ac.styling.domain.CardType;
import za.ac.styling.domain.User;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating CardPayment objects
 */
public class CardPaymentFactory {

    /**
     * Creates a new CardPayment with all information
     */
    public static CardPayment createCardPayment(String cardNumber, String cardHolder,
                                               String expiryDate, String cvv,
                                               CardType cardType, User user) {

        // Validate input data
        if (!ValidationHelper.isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }

        if (ValidationHelper.isNullOrEmpty(cardHolder)) {
            throw new IllegalArgumentException("Card holder name cannot be empty");
        }

        if (!ValidationHelper.isValidExpiryDate(expiryDate)) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/YY");
        }

        if (!ValidationHelper.isValidCVV(cvv)) {
            throw new IllegalArgumentException("Invalid CVV. Must be 3 or 4 digits");
        }

        if (cardType == null) {
            throw new IllegalArgumentException("Card type is required");
        }

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return CardPayment.builder()
                .cardNumber(cardNumber.replaceAll("\\s+", ""))
                .cardHolder(cardHolder.trim().toUpperCase())
                .expiryDate(expiryDate.trim())
                .cvv(cvv)
                .cardType(cardType)
                .user(user)
                .build();
    }



    /**
     * Creates a Visa card payment
     */
    public static CardPayment createVisaPayment(String cardNumber, String cardHolder,
                                               String expiryDate, String cvv, User user) {
        return createCardPayment(cardNumber, cardHolder, expiryDate, cvv, CardType.VISA, user);
    }

    /**
     * Creates a MasterCard payment
     */
    public static CardPayment createMasterCardPayment(String cardNumber, String cardHolder,
                                                     String expiryDate, String cvv, User user) {
        return createCardPayment(cardNumber, cardHolder, expiryDate, cvv, 
                                CardType.MASTERCARD, user);
    }

    /**
     * Creates an American Express card payment
     */
    public static CardPayment createAmexPayment(String cardNumber, String cardHolder,
                                               String expiryDate, String cvv, User user) {
        return createCardPayment(cardNumber, cardHolder, expiryDate, cvv, 
                                CardType.AMEX, user);
    }

    /**
     * Validates a CardPayment
     */
    public static boolean validateCardPayment(CardPayment payment) {
        if (payment == null) {
            return false;
        }
        return payment.validate();
    }

    /**
     * Creates a masked version of card number for display
     */
    public static String maskCardNumber(String cardNumber) {
        if (ValidationHelper.isNullOrEmpty(cardNumber) || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * Detects card type from card number
     */
    public static CardType detectCardType(String cardNumber) {
        if (ValidationHelper.isNullOrEmpty(cardNumber)) {
            return null;
        }

        cardNumber = cardNumber.replaceAll("\\s+", "");

        if (cardNumber.startsWith("4")) {
            return CardType.VISA;
        } else if (cardNumber.matches("^5[1-5].*")) {
            return CardType.MASTERCARD;
        } else if (cardNumber.matches("^3[47].*")) {
            return CardType.AMEX;
        }

        return null;
    }
}
