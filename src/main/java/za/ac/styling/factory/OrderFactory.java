package za.ac.styling.factory;

import za.ac.styling.domain.*;
import za.ac.styling.util.ValidationHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class OrderFactory {

    public static Order createOrder(User user, Address shippingAddress,
            ShippingMethod shippingMethod) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        if (shippingMethod == null) {
            throw new IllegalArgumentException("Shipping method is required");
        }

        String orderNumber = generateOrderNumber();

        return Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .billingAddress(shippingAddress)
                .shippingMethod(shippingMethod)
                .orderNumber(orderNumber)
                .items(new ArrayList<>())
                .orderDate(new Date())
                .subtotal(0.0)
                .shippingCost(0.0)
                .taxAmount(0.0)
                .discountAmount(0.0)
                .totalAmount(0.0)
                .invoiceEmailSent(false)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order createOrder(User user, Address shippingAddress, Address billingAddress,
            ShippingMethod shippingMethod) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        if (billingAddress == null) {
            throw new IllegalArgumentException("Billing address is required");
        }

        if (shippingMethod == null) {
            throw new IllegalArgumentException("Shipping method is required");
        }

        String orderNumber = generateOrderNumber();

        return Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .shippingMethod(shippingMethod)
                .orderNumber(orderNumber)
                .items(new ArrayList<>())
                .orderDate(new Date())
                .subtotal(0.0)
                .shippingCost(0.0)
                .taxAmount(0.0)
                .discountAmount(0.0)
                .totalAmount(0.0)
                .invoiceEmailSent(false)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order createOrderWithAmounts(User user, Address shippingAddress,
            ShippingMethod shippingMethod,
            double subtotal, double shippingCost,
            double taxAmount, double discountAmount) {

        Order order = createOrder(user, shippingAddress, shippingMethod);

        if (!ValidationHelper.isValidPrice(subtotal) || !ValidationHelper.isValidPrice(shippingCost) ||
                !ValidationHelper.isValidPrice(taxAmount) || !ValidationHelper.isValidPrice(discountAmount)) {
            throw new IllegalArgumentException("Invalid price amounts");
        }

        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(calculateTotalAmount(subtotal, shippingCost, taxAmount, discountAmount));

        return order;
    }

    public static Order createOrderWithPayment(User user, Address shippingAddress,
            ShippingMethod shippingMethod,
            Payment payment) {

        Order order = createOrder(user, shippingAddress, shippingMethod);

        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        order.setPayment(payment);
        order.setStatus(OrderStatus.PROCESSING);

        return order;
    }

    public static Order createOrderWithNotes(User user, Address shippingAddress,
            ShippingMethod shippingMethod, String notes) {

        Order order = createOrder(user, shippingAddress, shippingMethod);
        order.setNotes(notes);

        return order;
    }

    private static String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static double calculateTotalAmount(double subtotal, double shippingCost,
            double taxAmount, double discountAmount) {
        return subtotal + shippingCost + taxAmount - discountAmount;
    }
}
