package za.ac.styling.service;

import za.ac.styling.domain.Order;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.ShipmentStatus;

import java.util.List;
import java.util.Optional;

public interface ShipmentService extends IService<Shipment, Long> {

    Optional<Shipment> findByOrder(Order order);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByCarrier(String carrier);

    List<Shipment> findByStatus(ShipmentStatus status);

    boolean existsByTrackingNumber(String trackingNumber);

    Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status);

    Shipment updateTrackingNumber(Long shipmentId, String trackingNumber);
}
