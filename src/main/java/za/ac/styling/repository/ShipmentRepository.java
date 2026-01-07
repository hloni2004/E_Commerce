package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.ShipmentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrder(Order order);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByCarrier(String carrier);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByCarrierAndStatus(String carrier, ShipmentStatus status);

    boolean existsByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatusOrderByShippedDateDesc(ShipmentStatus status);
}
