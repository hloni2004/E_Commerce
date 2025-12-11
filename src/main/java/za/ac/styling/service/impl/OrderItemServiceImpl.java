package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.Product;
import za.ac.styling.repository.OrderItemRepository;
import za.ac.styling.service.OrderItemService;

import java.util.List;

/**
 * Service implementation for OrderItem entity
 */
@Service
public class OrderItemServiceImpl implements OrderItemService {

    private OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem read(Integer id) {
        return orderItemRepository.findById(id).orElse(null);
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> getAll() {
        return orderItemRepository.findAll();
    }

    @Override
    public List<OrderItem> findByOrder(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    @Override
    public List<OrderItem> findByOrderId(Integer orderId) {
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    @Override
    public List<OrderItem> findByProduct(Product product) {
        return orderItemRepository.findByProduct(product);
    }

    @Override
    public long countByOrder(Order order) {
        return orderItemRepository.countByOrder(order);
    }

    @Override
    public double calculateItemTotal(Integer orderItemId) {
        OrderItem orderItem = read(orderItemId);
        return orderItem != null ? orderItem.getTotalPrice() : 0.0;
    }

    @Override
    public void delete(Integer id) {
        orderItemRepository.deleteById(id);
    }
}