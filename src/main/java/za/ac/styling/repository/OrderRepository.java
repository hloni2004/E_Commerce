package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

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
    List<Order> findByUserUserId(Integer userId);

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
    List<Order> findByOrderDateBetween(Date startDate, Date endDate);

    /**
     * Find recent orders for a user
     */
    List<Order> findTop10ByUserOrderByOrderDateDesc(User user);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find all pending orders
     */
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
    
    /**
     * Check if user has purchased a specific product (completed orders only)
     */
    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM Order o " +
            "JOIN o.items oi WHERE o.user.userId = :userId AND oi.product.productId = :productId " +
            "AND o.status IN ('DELIVERED', 'COMPLETED')")
    boolean hasUserPurchasedProduct(@org.springframework.data.repository.query.Param("userId") Integer userId, 
                                   @org.springframework.data.repository.query.Param("productId") Integer productId);
}
