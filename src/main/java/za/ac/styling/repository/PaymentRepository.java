package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find all completed payments
     */
    List<Payment> findByStatusOrderByPaymentDateDesc(PaymentStatus status);

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);
}
