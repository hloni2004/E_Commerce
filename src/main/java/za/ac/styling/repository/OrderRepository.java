package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUser(User user);

    List<Order> findByUserUserId(Integer userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserAndStatus(User user, OrderStatus status);

    List<Order> findByOrderDateBetween(Date startDate, Date endDate);

    List<Order> findTop10ByUserOrderByOrderDateDesc(User user);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM Order o " +
            "JOIN o.items oi WHERE o.user.userId = :userId AND oi.product.productId = :productId " +
            "AND o.status IN ('DELIVERED', 'COMPLETED')")
    boolean hasUserPurchasedProduct(@org.springframework.data.repository.query.Param("userId") Integer userId, 
                                   @org.springframework.data.repository.query.Param("productId") Integer productId);
}
