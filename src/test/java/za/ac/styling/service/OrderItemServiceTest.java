package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.*;
import za.ac.styling.factory.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderItemServiceTest {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ShippingMethodService shippingMethodService;

    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        // Setup role
        Role role = roleService.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleService.create(RoleFactory.createRole("CUSTOMER")));

        // Setup user
        User user = UserFactory.createUser(
                "orderitemuser",
                "orderitem@example.com",
                "password123",
                "OrderItem",
                "User",
                "1112223333",
                role);
        user = userService.create(user);

        // Setup address
        Address address = AddressFactory.createShippingAddress(
                "123 Main St",
                "Cape Town",
                "Western Cape",
                "8000",
                "South Africa",
                user);
        address = addressService.create(address);

        // Setup shipping method
        ShippingMethod shippingMethod = ShippingMethod.builder()
                .methodName("Standard")
                .cost(50.0)
                .isActive(true)
                .build();
        shippingMethod = shippingMethodService.create(shippingMethod);

        // Setup order
        testOrder = OrderFactory.createOrder(user, address, shippingMethod);
        testOrder = orderService.create(testOrder);

        // Setup category and product
        Category category = CategoryFactory.createCategory("Electronics", "Electronic items");
        category = categoryService.create(category);

        testProduct = ProductFactory.createProduct(
                "Laptop",
                "HP Laptop",
                999.99,
                "SKU-LAPTOP-001",
                category);
        testProduct = productService.create(testProduct);

        // Setup order item
        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(2)
                .unitPrice(999.99)
                .priceAtPurchase(999.99)
                .totalPrice(1999.98)
                .build();
    }

    @Test
    void testCreate() {
        OrderItem created = orderItemService.create(testOrderItem);
        assertNotNull(created);
        assertNotNull(created.getOrderItemId());
        assertEquals(2, created.getQuantity());
        assertEquals(testProduct.getProductId(), created.getProduct().getProductId());
    }

    @Test
    void testRead() {
        OrderItem created = orderItemService.create(testOrderItem);
        OrderItem found = orderItemService.read(created.getOrderItemId());
        assertNotNull(found);
        assertEquals(created.getOrderItemId(), found.getOrderItemId());
    }

    @Test
    void testUpdate() {
        OrderItem created = orderItemService.create(testOrderItem);
        created.setQuantity(5);
        OrderItem updated = orderItemService.update(created);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testGetAll() {
        orderItemService.create(testOrderItem);
        List<OrderItem> items = orderItemService.getAll();
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    void testFindByOrder() {
        orderItemService.create(testOrderItem);
        List<OrderItem> items = orderItemService.findByOrder(testOrder);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(item -> item.getOrder().getOrderId().equals(testOrder.getOrderId())));
    }

    @Test
    void testFindByOrderId() {
        orderItemService.create(testOrderItem);
        List<OrderItem> items = orderItemService.findByOrderId(testOrder.getOrderId());
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    void testFindByProduct() {
        orderItemService.create(testOrderItem);
        List<OrderItem> items = orderItemService.findByProduct(testProduct);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(
                items.stream().anyMatch(item -> item.getProduct().getProductId().equals(testProduct.getProductId())));
    }

    @Test
    void testCountByOrder() {
        orderItemService.create(testOrderItem);
        long count = orderItemService.countByOrder(testOrder);
        assertTrue(count >= 1);
    }

    @Test
    void testCalculateItemTotal() {
        OrderItem created = orderItemService.create(testOrderItem);
        double total = orderItemService.calculateItemTotal(created.getOrderItemId());
        assertEquals(1999.98, total, 0.01);
    }

    @Test
    void testCreateMultipleOrderItems() {
        OrderItem item1 = orderItemService.create(testOrderItem);

        OrderItem item2 = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(1)
                .unitPrice(50.0)
                .priceAtPurchase(50.0)
                .totalPrice(50.0)
                .build();
        OrderItem created2 = orderItemService.create(item2);

        assertNotNull(item1);
        assertNotNull(created2);

        List<OrderItem> items = orderItemService.findByOrder(testOrder);
        assertTrue(items.size() >= 2);
    }
}
