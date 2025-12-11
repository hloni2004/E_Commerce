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
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order read(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order update(Order order) {
        return orderRepository.save(order);
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
}
