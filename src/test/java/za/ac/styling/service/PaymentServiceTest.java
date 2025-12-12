package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Payment;
import za.ac.styling.domain.PaymentStatus;
import za.ac.styling.factory.PaymentFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = PaymentFactory.createPayment(500.00, "ZAR", null);
    }

    @Test
    void testCreate() {
        Payment created = paymentService.create(testPayment);
        assertNotNull(created);
        assertNotNull(created.getPaymentId());
        assertEquals(500.00, created.getAmount());
    }

    @Test
    void testRead() {
        Payment created = paymentService.create(testPayment);
        Payment found = paymentService.read(created.getPaymentId());
        assertNotNull(found);
        assertEquals(created.getPaymentId(), found.getPaymentId());
    }

    @Test
    void testUpdate() {
        Payment created = paymentService.create(testPayment);
        created.setAmount(600.00);
        Payment updated = paymentService.update(created);
        assertNotNull(updated);
        assertEquals(600.00, updated.getAmount());
    }

    @Test
    void testGetAll() {
        paymentService.create(testPayment);
        List<Payment> payments = paymentService.getAll();
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
    }

    @Test
    void testFindByTransactionId() {
        Payment created = paymentService.create(testPayment);
        Optional<Payment> found = paymentService.findByTransactionId(created.getTransactionId());
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByStatus() {
        testPayment.setStatus(PaymentStatus.PENDING);
        paymentService.create(testPayment);
        List<Payment> payments = paymentService.findByStatus(PaymentStatus.PENDING);
        assertNotNull(payments);
        assertFalse(payments.isEmpty());
    }

    @Test
    void testExistsByTransactionId() {
        Payment created = paymentService.create(testPayment);
        assertTrue(paymentService.existsByTransactionId(created.getTransactionId()));
    }

    @Test
    void testUpdatePaymentStatus() {
        Payment created = paymentService.create(testPayment);
        Payment updated = paymentService.updatePaymentStatus(created.getPaymentId(), PaymentStatus.COMPLETED);
        assertNotNull(updated);
        assertEquals(PaymentStatus.COMPLETED, updated.getStatus());
    }
}
