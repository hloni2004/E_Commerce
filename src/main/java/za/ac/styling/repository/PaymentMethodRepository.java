package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PaymentMethod;
import za.ac.styling.domain.User;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUser(User user);

    List<PaymentMethod> findByUserUserId(Integer userId);

    void deleteByUser(User user);
}
