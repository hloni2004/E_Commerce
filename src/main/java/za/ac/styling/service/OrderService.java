package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderService extends IService<Order, Integer> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUser(User user);

    List<Order> findByUserId(Integer userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserAndStatus(User user, OrderStatus status);

    List<Order> findByDateRange(Date startDate, Date endDate);

    List<Order> findRecentOrdersByUser(User user);

    Order updateOrderStatus(Integer orderId, OrderStatus status);

    double calculateOrderTotal(Integer orderId);

    Order createOrderWithPromo(Order order, java.util.Map<Integer, Integer> productQuantities, String promoCode,
            Integer userId);
}
