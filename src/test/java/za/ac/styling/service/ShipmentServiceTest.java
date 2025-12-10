package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.Shipment;
import za.ac.styling.domain.ShipmentStatus;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.domain.User;
import za.ac.styling.factory.AddressFactory;
import za.ac.styling.factory.OrderFactory;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.ShipmentFactory;
import za.ac.styling.factory.UserFactory;
import za.ac.styling.repository.ShippingMethodRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShipmentServiceTest {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ShippingMethodRepository shippingMethodRepository;

    private Order testOrder;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        Role role = RoleFactory.createRole("CUSTOMER");
        role = roleService.create(role);

        User user = UserFactory.createUser(
                "shipmentuser",
                "shipment@example.com",
                "password123",
                "Shipment",
                "User",
                "4445556666",
                role
        );
        user = userService.create(user);

        Address address = AddressFactory.createAddress(
                "456 Oak St",
                "Los Angeles",
                "CA",
                "90001",
                "USA",
                AddressType.SHIPPING,
                user
        );
        address = addressService.create(address);

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .name("Express Shipping")
                .description("2-3 business days")
                .cost(25.00)
                .estimatedDays(3)
                .isActive(true)
                .build();
        shippingMethod = shippingMethodRepository.save(shippingMethod);

        testOrder = OrderFactory.createOrder(user, address, shippingMethod);
        testOrder = orderService.create(testOrder);

        testShipment = ShipmentFactory.createShipment(testOrder, shippingMethod, "DHL");
    }

    @Test
    void testCreate() {
        Shipment created = shipmentService.create(testShipment);
        assertNotNull(created);
        assertNotNull(created.getShipmentId());
        assertEquals("DHL", created.getCarrier());
    }

    @Test
    void testRead() {
        Shipment created = shipmentService.create(testShipment);
        Shipment found = shipmentService.read(created.getShipmentId());
        assertNotNull(found);
        assertEquals(created.getShipmentId(), found.getShipmentId());
    }

    @Test
    void testUpdate() {
        Shipment created = shipmentService.create(testShipment);
        created.setCarrier("FedEx");
        Shipment updated = shipmentService.update(created);
        assertNotNull(updated);
        assertEquals("FedEx", updated.getCarrier());
    }

    @Test
    void testGetAll() {
        shipmentService.create(testShipment);
        List<Shipment> shipments = shipmentService.getAll();
        assertNotNull(shipments);
        assertFalse(shipments.isEmpty());
    }

    @Test
    void testFindByOrder() {
        shipmentService.create(testShipment);
        Optional<Shipment> found = shipmentService.findByOrder(testOrder);
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByTrackingNumber() {
        Shipment created = shipmentService.create(testShipment);
        Optional<Shipment> found = shipmentService.findByTrackingNumber(created.getTrackingNumber());
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByCarrier() {
        shipmentService.create(testShipment);
        List<Shipment> shipments = shipmentService.findByCarrier("DHL");
        assertNotNull(shipments);
        assertFalse(shipments.isEmpty());
    }

    @Test
    void testFindByStatus() {
        testShipment.setStatus(ShipmentStatus.CREATED);
        shipmentService.create(testShipment);
        List<Shipment> shipments = shipmentService.findByStatus(ShipmentStatus.CREATED);
        assertNotNull(shipments);
        assertFalse(shipments.isEmpty());
    }

    @Test
    void testUpdateShipmentStatus() {
        Shipment created = shipmentService.create(testShipment);
        Shipment updated = shipmentService.updateShipmentStatus(created.getShipmentId(), ShipmentStatus.SHIPPED);
        assertNotNull(updated);
        assertEquals(ShipmentStatus.SHIPPED, updated.getStatus());
    }

    @Test
    void testUpdateTrackingNumber() {
        Shipment created = shipmentService.create(testShipment);
        Shipment updated = shipmentService.updateTrackingNumber(created.getShipmentId(), "NEW123456");
        assertNotNull(updated);
        assertEquals("NEW123456", updated.getTrackingNumber());
    }
}
