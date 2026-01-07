package za.ac.styling.factory;

import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.domain.ShipmentStatus;
import za.ac.styling.util.ValidationHelper;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShipmentFactory {

    public static Shipment createShipment(Order order, ShippingMethod shippingMethod,
            String carrier) {

        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        if (shippingMethod == null) {
            throw new IllegalArgumentException("Shipping method is required");
        }

        if (ValidationHelper.isNullOrEmpty(carrier)) {
            throw new IllegalArgumentException("Carrier is required");
        }

        String trackingNumber = generateTrackingNumber();

        return Shipment.builder()
                .order(order)
                .shippingMethod(shippingMethod)
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.CREATED)
                .build();
    }

    public static Shipment createShipmentWithTracking(Order order, ShippingMethod shippingMethod,
            String carrier, String trackingNumber) {

        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        if (shippingMethod == null) {
            throw new IllegalArgumentException("Shipping method is required");
        }

        if (ValidationHelper.isNullOrEmpty(carrier)) {
            throw new IllegalArgumentException("Carrier is required");
        }

        if (ValidationHelper.isNullOrEmpty(trackingNumber)) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        return Shipment.builder()
                .order(order)
                .shippingMethod(shippingMethod)
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.CREATED)
                .build();
    }

    public static Shipment createShippedShipment(Order order, ShippingMethod shippingMethod,
            String carrier, String trackingNumber,
            LocalDateTime shippedDate) {

        Shipment shipment = createShipmentWithTracking(order, shippingMethod, carrier, trackingNumber);

        if (shippedDate == null) {
            shippedDate = LocalDateTime.now();
        }

        shipment.setShippedDate(shippedDate);
        shipment.setStatus(ShipmentStatus.SHIPPED);

        return shipment;
    }

    public static Shipment createShipmentWithEstimatedDelivery(Order order,
            ShippingMethod shippingMethod,
            String carrier,
            LocalDateTime estimatedDelivery) {

        Shipment shipment = createShipment(order, shippingMethod, carrier);

        if (estimatedDelivery == null) {
            throw new IllegalArgumentException("Estimated delivery date is required");
        }

        if (estimatedDelivery.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Estimated delivery date cannot be in the past");
        }

        shipment.setEstimatedDelivery(estimatedDelivery);

        return shipment;
    }

    public static Shipment createDeliveredShipment(Order order, ShippingMethod shippingMethod,
            String carrier, String trackingNumber,
            LocalDateTime shippedDate,
            LocalDateTime actualDelivery) {

        Shipment shipment = createShippedShipment(order, shippingMethod, carrier,
                trackingNumber, shippedDate);

        if (actualDelivery == null) {
            actualDelivery = LocalDateTime.now();
        }

        shipment.setActualDelivery(actualDelivery);
        shipment.setStatus(ShipmentStatus.DELIVERED);

        return shipment;
    }

    public static Shipment createInTransitShipment(Order order, ShippingMethod shippingMethod,
            String carrier, String trackingNumber) {

        Shipment shipment = createShipmentWithTracking(order, shippingMethod, carrier, trackingNumber);
        shipment.setShippedDate(LocalDateTime.now());
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        return shipment;
    }

    public static Shipment createFailedShipment(Order order, ShippingMethod shippingMethod,
            String carrier, String trackingNumber) {

        Shipment shipment = createShipmentWithTracking(order, shippingMethod, carrier, trackingNumber);
        shipment.setStatus(ShipmentStatus.FAILED);

        return shipment;
    }

    private static String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
