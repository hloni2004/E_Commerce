package za.ac.styling.service;

import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Payment entity
 */
public interface PaymentService extends IService<Payment, Integer> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Update payment status
     */
    Payment updatePaymentStatus(Integer paymentId, PaymentStatus status);
}
