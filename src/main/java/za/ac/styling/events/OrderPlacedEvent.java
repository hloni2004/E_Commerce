package za.ac.styling.events;

import org.springframework.context.ApplicationEvent;
import za.ac.styling.domain.Order;

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
