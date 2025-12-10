package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order entity
 */
public interface OrderService extends IService<Order, Integer> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders for a user
     */
    List<Order> findByUser(User user);

    /**
     * Find all orders for a user by user ID
     */
    List<Order> findByUserId(Integer userId);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by user and status
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    /**
     * Find orders by date range
     */
    List<Order> findByDateRange(Date startDate, Date endDate);

    /**
     * Find recent orders for a user
     */
    List<Order> findRecentOrdersByUser(User user);

    /**
     * Update order status
     */
    Order updateOrderStatus(Integer orderId, OrderStatus status);

    /**
     * Calculate order total
     */
    double calculateOrderTotal(Integer orderId);
}
