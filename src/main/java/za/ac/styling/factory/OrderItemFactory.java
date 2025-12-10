package za.ac.styling.factory;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

/**
 * Factory class responsible for creating OrderItem objects
 */
public class OrderItemFactory {

    /**
     * Creates a new OrderItem with all required information
     */
    public static OrderItem createOrderItem(Order order, Product product, ProductColour colour,
                                           ProductColourSize size, int quantity, 
                                           double unitPrice) {

        // Validate input data
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        if (colour == null) {
            throw new IllegalArgumentException("Product colour is required");
        }

        if (size == null) {
            throw new IllegalArgumentException("Product size is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        double totalPrice = unitPrice * quantity;

        return OrderItem.builder()
                .order(order)
                .product(product)
                .colour(colour)
                .size(size)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .priceAtPurchase(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * Creates a new OrderItem using the current product base price
     */
    public static OrderItem createOrderItem(Order order, Product product, ProductColour colour,
                                           ProductColourSize size, int quantity) {
        return createOrderItem(order, product, colour, size, quantity, product.getBasePrice());
    }

    /**
     * Creates OrderItem from CartItem with current product price
     */
    public static OrderItem createFromCartItem(Order order, za.ac.styling.domain.CartItem cartItem) {
        if (cartItem == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }

        return createOrderItem(
                order,
                cartItem.getProduct(),
                cartItem.getColour(),
                cartItem.getSize(),
                cartItem.getQuantity(),
                cartItem.getProduct().getBasePrice()
        );
    }

    /**
     * Recalculates the total price for an OrderItem
     */
    public static OrderItem recalculateTotal(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem cannot be null");
        }

        double totalPrice = orderItem.getUnitPrice() * orderItem.getQuantity();
        orderItem.setTotalPrice(totalPrice);
        return orderItem;
    }
}
