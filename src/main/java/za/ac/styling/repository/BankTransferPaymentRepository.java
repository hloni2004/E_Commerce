package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.BankTransferPayment;
import za.ac.styling.domain.User;

import java.util.List;

@Repository
public interface BankTransferPaymentRepository extends JpaRepository<BankTransferPayment, Long> {

    List<BankTransferPayment> findByUser(User user);

    List<BankTransferPayment> findByBankName(String bankName);
}
