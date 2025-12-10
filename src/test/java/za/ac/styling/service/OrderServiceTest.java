package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.domain.User;
import za.ac.styling.factory.AddressFactory;
import za.ac.styling.factory.OrderFactory;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.UserFactory;
import za.ac.styling.repository.ShippingMethodRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

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

    private User testUser;
    private Order testOrder;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        Role role = RoleFactory.createRole("CUSTOMER");
        role = roleService.create(role);

        testUser = UserFactory.createUser(
                "orderuser",
                "order@example.com",
                "password123",
                "Order",
                "User",
                "2223334444",
                role
        );
        testUser = userService.create(testUser);

        testAddress = AddressFactory.createAddress(
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA",
                AddressType.SHIPPING,
                testUser
        );
        testAddress = addressService.create(testAddress);

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .name("Standard Shipping")
                .description("5-7 business days")
                .cost(10.00)
                .estimatedDays(7)
                .isActive(true)
                .build();
        shippingMethod = shippingMethodRepository.save(shippingMethod);

        testOrder = OrderFactory.createOrder(testUser, testAddress, shippingMethod);
    }

    @Test
    void testCreate() {
        Order created = orderService.create(testOrder);
        assertNotNull(created);
        assertNotNull(created.getOrderId());
        assertEquals(testUser.getUserId(), created.getUser().getUserId());
    }

    @Test
    void testRead() {
        Order created = orderService.create(testOrder);
        Order found = orderService.read(created.getOrderId());
        assertNotNull(found);
        assertEquals(created.getOrderId(), found.getOrderId());
    }

    @Test
    void testUpdate() {
        Order created = orderService.create(testOrder);
        created.setTotalAmount(600.00);
        Order updated = orderService.update(created);
        assertNotNull(updated);
        assertEquals(600.00, updated.getTotalAmount());
    }

    @Test
    void testGetAll() {
        orderService.create(testOrder);
        List<Order> orders = orderService.getAll();
        assertNotNull(orders);
        assertFalse(orders.isEmpty());
    }

    @Test
    void testFindByOrderNumber() {
        Order created = orderService.create(testOrder);
        Optional<Order> found = orderService.findByOrderNumber(created.getOrderNumber());
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByUser() {
        orderService.create(testOrder);
        List<Order> orders = orderService.findByUser(testUser);
        assertNotNull(orders);
        assertFalse(orders.isEmpty());
    }

    @Test
    void testFindByStatus() {
        testOrder.setStatus(OrderStatus.PENDING);
        orderService.create(testOrder);
        List<Order> orders = orderService.findByStatus(OrderStatus.PENDING);
        assertNotNull(orders);
        assertFalse(orders.isEmpty());
    }

    @Test
    void testUpdateOrderStatus() {
        Order created = orderService.create(testOrder);
        Order updated = orderService.updateOrderStatus(created.getOrderId(), OrderStatus.CONFIRMED);
        assertNotNull(updated);
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void testCalculateOrderTotal() {
        Order created = orderService.create(testOrder);
        double total = orderService.calculateOrderTotal(created.getOrderId());
        assertEquals(500.00, total);
    }
}
