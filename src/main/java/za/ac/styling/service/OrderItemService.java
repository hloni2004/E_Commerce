package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.Product;

import java.util.List;

public interface OrderItemService extends IService<OrderItem, Integer> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Integer orderId);

    List<OrderItem> findByProduct(Product product);

    long countByOrder(Order order);

    double calculateItemTotal(Integer orderItemId);
}