package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.ShipmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shipment entity
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

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
     * Find shipments by carrier and status
     */
    List<Shipment> findByCarrierAndStatus(String carrier, ShipmentStatus status);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Find all in-transit shipments
     */
    List<Shipment> findByStatusOrderByShippedDateDesc(ShipmentStatus status);
}
