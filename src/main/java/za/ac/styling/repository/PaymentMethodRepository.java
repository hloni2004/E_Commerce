package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PaymentMethod;
import za.ac.styling.domain.User;

import java.util.List;

/**
 * Repository interface for PaymentMethod entity
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find all payment methods for a user
     */
    List<PaymentMethod> findByUser(User user);

    /**
     * Find all payment methods for a user by user ID
     */
    List<PaymentMethod> findByUserUserId(Integer userId);

    /**
     * Delete all payment methods for a user
     */
    void deleteByUser(User user);
}
