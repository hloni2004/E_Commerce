package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.Product;

import java.util.List;

/**
 * Service interface for OrderItem entity
 */
public interface OrderItemService extends IService<OrderItem, Integer> {

    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find all items in an order by order ID
     */
    List<OrderItem> findByOrderId(Integer orderId);

    /**
     * Find all orders containing a specific product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Count items in an order
     */
    long countByOrder(Order order);

    /**
     * Calculate total for an order item
     */
    double calculateItemTotal(Integer orderItemId);
}