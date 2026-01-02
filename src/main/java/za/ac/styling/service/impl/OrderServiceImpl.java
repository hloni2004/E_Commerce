package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.User;
import za.ac.styling.repository.OrderRepository;
import za.ac.styling.service.OrderService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Order entity
 */
@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;

    @Autowired
    private za.ac.styling.service.PromoCodeService promoService;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order create(Order order) {
        Order saved = orderRepository.save(order);
        // Fire OrderPlacedEvent for listeners (email, analytics, etc.)
        try {
            eventPublisher.publishEvent(new za.ac.styling.events.OrderPlacedEvent(this, saved));
        } catch (Exception ex) {
            System.err.println(
                    "Failed to publish OrderPlacedEvent for order " + saved.getOrderNumber() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return saved;
    }

    @Override
    public Order read(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order update(Order order) {
        Order saved = orderRepository.save(order);
        try {
            eventPublisher.publishEvent(new za.ac.styling.events.OrderPlacedEvent(this, saved));
        } catch (Exception ex) {
            System.err.println("Failed to publish OrderPlacedEvent for order "
                    + (saved != null ? saved.getOrderNumber() : "<unknown>") + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return saved;
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        return orderRepository.findByUserUserId(userId);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> findByUserAndStatus(User user, OrderStatus status) {
        return orderRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<Order> findByDateRange(Date startDate, Date endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    @Override
    public List<Order> findRecentOrdersByUser(User user) {
        return orderRepository.findTop10ByUserOrderByOrderDateDesc(user);
    }

    @Override
    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = read(orderId);
        if (order != null) {
            order.setStatus(status);
            return update(order);
        }
        return null;
    }

    @Override
    public double calculateOrderTotal(Integer orderId) {
        Order order = read(orderId);
        return order != null ? order.getTotalAmount() : 0.0;
    }

    @Override
    public void delete(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Order createOrderWithPromo(Order order, java.util.Map<Integer, Integer> productQuantities, String promoCode,
            Integer userId) {
        // Compute subtotal (order.subtotal should already be set by caller, but we
        // guard)
        long subtotalCents = Math.round(order.getSubtotal() * 100);

        // If promo code present, validate and preview discount
        if (promoCode != null && !promoCode.isEmpty()) {
            var preview = promoService.processPromo(promoCode, userId, productQuantities, subtotalCents, false, null);
            if (!preview.isApplied()) {
                throw new IllegalArgumentException("Promo invalid: " + preview.getMessage());
            }
            order.setDiscountAmount(preview.getDiscountAmountCents() / 100.0);
            order.setTotalAmount(preview.getFinalTotalCents() / 100.0 + order.getShippingCost() + order.getTaxAmount());
        }

        Order saved = orderRepository.save(order);

        // Finalize promo usage atomically with order persistence
        if (promoCode != null && !promoCode.isEmpty()) {
            var finalizeRes = promoService.processPromo(promoCode, userId, productQuantities, subtotalCents, true,
                    saved.getOrderId());
            if (!finalizeRes.isApplied()) {
                throw new IllegalStateException("Failed to finalize promo usage: " + finalizeRes.getMessage());
            }
        }

        // Publish an OrderPlacedEvent so other components (email, analytics,
        // fulfillment)
        // can react asynchronously without blocking the request thread.
        try {
            eventPublisher.publishEvent(new za.ac.styling.events.OrderPlacedEvent(this, saved));
        } catch (Exception ex) {
            // Protect order flow from event publishing failures
            System.err.println(
                    "Failed to publish OrderPlacedEvent for order " + saved.getOrderNumber() + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        return saved;
    }
}
