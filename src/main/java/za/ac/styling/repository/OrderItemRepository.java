package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.Product;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find all items in an order by order ID
     */
    List<OrderItem> findByOrderOrderId(Integer orderId);

    /**
     * Find all orders containing a specific product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Count items in an order
     */
    long countByOrder(Order order);
}
