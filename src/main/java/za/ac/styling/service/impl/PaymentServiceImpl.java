package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentStatus;
import za.ac.styling.repository.PaymentRepository;
import za.ac.styling.service.PaymentService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Payment entity
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment read(Integer id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Payment update(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public boolean existsByTransactionId(String transactionId) {
        return paymentRepository.existsByTransactionId(transactionId);
    }

    @Override
    public Payment updatePaymentStatus(Integer paymentId, PaymentStatus status) {
        Payment payment = read(paymentId);
        if (payment != null) {
            payment.setStatus(status);
            return update(payment);
        }
        return null;
    }
}
