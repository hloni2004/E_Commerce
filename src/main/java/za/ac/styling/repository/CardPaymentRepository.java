package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.CardPayment;
import za.ac.styling.domain.CardType;
import za.ac.styling.domain.User;

import java.util.List;

/**
 * Repository interface for CardPayment entity
 */
@Repository
public interface CardPaymentRepository extends JpaRepository<CardPayment, Long> {

    /**
     * Find all card payments for a user
     */
    List<CardPayment> findByUser(User user);

    /**
     * Find card payments by card type
     */
    List<CardPayment> findByCardType(CardType cardType);

    /**
     * Find card payments by user and card type
     */
    List<CardPayment> findByUserAndCardType(User user, CardType cardType);
}
