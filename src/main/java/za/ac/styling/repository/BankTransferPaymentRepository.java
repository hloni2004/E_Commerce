package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.BankTransferPayment;
import za.ac.styling.domain.User;

import java.util.List;

/**
 * Repository interface for BankTransferPayment entity
 */
@Repository
public interface BankTransferPaymentRepository extends JpaRepository<BankTransferPayment, Long> {

    /**
     * Find all bank transfer payments for a user
     */
    List<BankTransferPayment> findByUser(User user);

    /**
     * Find bank transfers by bank name
     */
    List<BankTransferPayment> findByBankName(String bankName);
}
