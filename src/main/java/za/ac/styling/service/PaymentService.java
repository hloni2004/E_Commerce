package za.ac.styling.service;

import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentService extends IService<Payment, Integer> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByTransactionId(String transactionId);

    Payment updatePaymentStatus(Integer paymentId, PaymentStatus status);
}
