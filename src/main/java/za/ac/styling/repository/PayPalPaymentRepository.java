package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PayPalPayment;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PayPalPayment entity
 */
@Repository
public interface PayPalPaymentRepository extends JpaRepository<PayPalPayment, Long> {

    /**
     * Find all PayPal payments for a user
     */
    List<PayPalPayment> findByUser(User user);

    /**
     * Find PayPal payment by email
     */
    Optional<PayPalPayment> findByEmail(String email);

    /**
     * Find PayPal payment by payer ID
     */
    Optional<PayPalPayment> findByPayerId(String payerId);
}
