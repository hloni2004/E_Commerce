package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.ShipmentStatus;
import za.ac.styling.repository.ShipmentRepository;
import za.ac.styling.service.ShipmentService;

import java.util.List;
import java.util.Optional;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private ShipmentRepository shipmentRepository;

    @Autowired
    public ShipmentServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Shipment create(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment read(Long id) {
        return shipmentRepository.findById(id).orElse(null);
    }

    @Override
    public Shipment update(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    @Override
    public List<Shipment> getAll() {
        return shipmentRepository.findAll();
    }

    @Override
    public Optional<Shipment> findByOrder(Order order) {
        return shipmentRepository.findByOrder(order);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    @Override
    public List<Shipment> findByCarrier(String carrier) {
        return shipmentRepository.findByCarrier(carrier);
    }

    @Override
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatus(status);
    }

    @Override
    public boolean existsByTrackingNumber(String trackingNumber) {
        return shipmentRepository.existsByTrackingNumber(trackingNumber);
    }

    @Override
    public Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status) {
        Shipment shipment = read(shipmentId);
        if (shipment != null) {
            shipment.setStatus(status);
            return update(shipment);
        }
        return null;
    }

    @Override
    public Shipment updateTrackingNumber(Long shipmentId, String trackingNumber) {
        Shipment shipment = read(shipmentId);
        if (shipment != null) {
            shipment.setTrackingNumber(trackingNumber);
            return update(shipment);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        shipmentRepository.deleteById(id);
    }
}
