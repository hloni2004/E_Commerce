package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PayPalPayment;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayPalPaymentRepository extends JpaRepository<PayPalPayment, Long> {

    List<PayPalPayment> findByUser(User user);

    Optional<PayPalPayment> findByEmail(String email);

    Optional<PayPalPayment> findByPayerId(String payerId);
}
