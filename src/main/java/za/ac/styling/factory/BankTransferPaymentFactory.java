package za.ac.styling.factory;

import za.ac.styling.domain.BankTransferPayment;
import za.ac.styling.domain.User;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating BankTransferPayment objects
 */
public class BankTransferPaymentFactory {

    /**
     * Creates a new BankTransferPayment with all information
     */
    public static BankTransferPayment createBankTransferPayment(String bankName, 
                                                                String accountNumber,
                                                                String routingNumber, 
                                                                User user) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(bankName)) {
            throw new IllegalArgumentException("Bank name cannot be empty");
        }

        if (ValidationHelper.isNullOrEmpty(accountNumber)) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }

        if (ValidationHelper.isNullOrEmpty(routingNumber)) {
            throw new IllegalArgumentException("Routing number cannot be empty");
        }

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return BankTransferPayment.builder()
                .bankName(bankName.trim())
                .accountNumber(accountNumber.trim())
                .routingNumber(routingNumber.trim())
                .user(user)
                .build();
    }



    /**
     * Validates a BankTransferPayment
     */
    public static boolean validateBankTransferPayment(BankTransferPayment payment) {
        if (payment == null) {
            return false;
        }
        return payment.validate();
    }

    /**
     * Creates a masked version of account number for display
     */
    public static String maskAccountNumber(String accountNumber) {
        if (ValidationHelper.isNullOrEmpty(accountNumber) || accountNumber.length() < 4) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    /**
     * Updates bank information
     */
    public static BankTransferPayment updateBankInfo(BankTransferPayment payment, 
                                                     String bankName) {
        if (payment == null) {
            throw new IllegalArgumentException("BankTransferPayment cannot be null");
        }

        if (ValidationHelper.isNullOrEmpty(bankName)) {
            throw new IllegalArgumentException("Bank name cannot be empty");
        }

        payment.setBankName(bankName.trim());
        return payment;
    }
}
