package za.ac.styling.factory;

import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentMethod;
import za.ac.styling.domain.PaymentStatus;
import za.ac.styling.util.ValidationHelper;

import java.time.LocalDate;

public class PaymentFactory {

    public static Payment createPayment(double amount, String currency, PaymentMethod paymentMethod) {

        if (!ValidationHelper.isValidPrice(amount)) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        if (ValidationHelper.isNullOrEmpty(currency)) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        return Payment.builder()
                .amount(amount)
                .currency(currency.toUpperCase())
                .paymentMethod(paymentMethod)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public static Payment createPaymentWithTransaction(double amount, String currency,
            PaymentMethod paymentMethod,
            String transactionId) {

        if (ValidationHelper.isNullOrEmpty(transactionId)) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        Payment payment = createPayment(amount, currency, paymentMethod);
        payment.setTransactionId(transactionId);

        return payment;
    }

    public static Payment createSuccessfulPayment(double amount, String currency,
            PaymentMethod paymentMethod,
            String transactionId) {

        Payment payment = createPaymentWithTransaction(amount, currency, paymentMethod, transactionId);
        payment.setStatus(PaymentStatus.COMPLETED);

        return payment;
    }

    public static Payment createFailedPayment(double amount, String currency,
            PaymentMethod paymentMethod,
            String failureReason) {

        if (ValidationHelper.isNullOrEmpty(failureReason)) {
            throw new IllegalArgumentException("Failure reason is required for failed payment");
        }

        Payment payment = createPayment(amount, currency, paymentMethod);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);

        return payment;
    }

    public static Payment createRefundedPayment(double amount, String currency,
            PaymentMethod paymentMethod,
            String transactionId) {

        Payment payment = createPaymentWithTransaction(amount, currency, paymentMethod, transactionId);
        payment.setStatus(PaymentStatus.REFUNDED);

        return payment;
    }

    public static Payment createPendingPayment(double amount, String currency,
            PaymentMethod paymentMethod) {

        Payment payment = createPayment(amount, currency, paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);

        return payment;
    }
}
