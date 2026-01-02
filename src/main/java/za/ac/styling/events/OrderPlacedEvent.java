package za.ac.styling.events;

import org.springframework.context.ApplicationEvent;
import za.ac.styling.domain.Order;

/**
 * Event published when an order is placed and persisted.
 */
public class OrderPlacedEvent extends ApplicationEvent {
    private final Order order;

    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
