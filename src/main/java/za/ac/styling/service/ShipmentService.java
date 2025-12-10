package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.ShipmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Shipment entity
 */
public interface ShipmentService extends IService<Shipment, Long> {

    /**
     * Find shipment by order
     */
    Optional<Shipment> findByOrder(Order order);

    /**
     * Find shipment by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Find shipments by carrier
     */
    List<Shipment> findByCarrier(String carrier);

    /**
     * Find shipments by status
     */
    List<Shipment> findByStatus(ShipmentStatus status);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Update shipment status
     */
    Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status);

    /**
     * Update tracking number
     */
    Shipment updateTrackingNumber(Long shipmentId, String trackingNumber);
}
