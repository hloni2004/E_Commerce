package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.CardPayment;
import za.ac.styling.domain.CardType;
import za.ac.styling.domain.User;

import java.util.List;

@Repository
public interface CardPaymentRepository extends JpaRepository<CardPayment, Long> {

    List<CardPayment> findByUser(User user);

    List<CardPayment> findByCardType(CardType cardType);

    List<CardPayment> findByUserAndCardType(User user, CardType cardType);
}
