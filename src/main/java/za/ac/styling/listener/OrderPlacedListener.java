package za.ac.styling.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.ac.styling.events.OrderPlacedEvent;
import za.ac.styling.service.EmailService;
import za.ac.styling.service.OrderService;
import za.ac.styling.domain.Order;

/**
 * Listens for OrderPlacedEvent and sends invoice email asynchronously.
 */
@Component
public class OrderPlacedListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedListener.class);

    private final EmailService emailService;
    private final OrderService orderService;

    @Autowired
    public OrderPlacedListener(EmailService emailService, OrderService orderService) {
        this.emailService = emailService;
        this.orderService = orderService;
    }

    @Async
    @EventListener
    @org.springframework.transaction.annotation.Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        Order eventOrder = event.getOrder();
        if (eventOrder == null || eventOrder.getOrderId() == null) {
            logger.warn("Received OrderPlacedEvent with null order or id");
            return;
        }
        try {
            // Reload the order in a transactional context to allow lazy-loading of items
            // and related data
            Order order = orderService.read(eventOrder.getOrderId());
            if (order == null) {
                logger.warn("Order with id {} not found when handling OrderPlacedEvent", eventOrder.getOrderId());
                return;
            }

            // Avoid duplicate sends
            if (order.isInvoiceEmailSent()) {
                logger.info("Invoice already sent for order {} - skipping", order.getOrderNumber());
                return;
            }

            if (order.getUser() != null && order.getUser().getEmail() != null) {
                emailService.sendOrderInvoice(order.getUser(), order);
                order.setInvoiceEmailSent(true);
                orderService.update(order); // mark sent
                logger.info("Invoice email sent for order {}", order.getOrderNumber());
            } else {
                logger.warn("Order {} has no user or email; skipping invoice email", order.getOrderNumber());
            }
        } catch (Exception e) {
            logger.error("Failed to send invoice for order {}: {}",
                    eventOrder != null ? eventOrder.getOrderNumber() : "<unknown>", e.getMessage(), e);
            // Do not rethrow; email failures should not break main flow
        }
    }
}
