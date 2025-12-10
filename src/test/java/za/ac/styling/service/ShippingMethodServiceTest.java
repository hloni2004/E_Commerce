package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.ShippingMethod;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShippingMethodServiceTest {

    @Autowired
    private ShippingMethodService shippingMethodService;

    private ShippingMethod testShippingMethod;

    @BeforeEach
    void setUp() {
        testShippingMethod = ShippingMethod.builder()
                .name("Standard Shipping")
                .description("Standard delivery in 5-7 business days")
                .cost(50.0)
                .estimatedDays(5)
                .isActive(true)
                .build();
    }

    @Test
    void testCreate() {
        ShippingMethod created = shippingMethodService.create(testShippingMethod);
        assertNotNull(created);
        assertNotNull(created.getMethodId());
        assertEquals("Standard Shipping", created.getName());
        assertEquals(50.0, created.getCost());
    }

    @Test
    void testRead() {
        ShippingMethod created = shippingMethodService.create(testShippingMethod);
        ShippingMethod found = shippingMethodService.read(created.getMethodId());
        assertNotNull(found);
        assertEquals(created.getMethodId(), found.getMethodId());
    }

    @Test
    void testUpdate() {
        ShippingMethod created = shippingMethodService.create(testShippingMethod);
        created.setCost(75.0);
        ShippingMethod updated = shippingMethodService.update(created);
        assertNotNull(updated);
        assertEquals(75.0, updated.getCost());
    }

    @Test
    void testGetAll() {
        shippingMethodService.create(testShippingMethod);
        List<ShippingMethod> methods = shippingMethodService.getAll();
        assertNotNull(methods);
        assertFalse(methods.isEmpty());
    }

    @Test
    void testFindByName() {
        shippingMethodService.create(testShippingMethod);
        Optional<ShippingMethod> found = shippingMethodService.findByName("Standard Shipping");
        assertTrue(found.isPresent());
        assertEquals("Standard Shipping", found.get().getName());
    }

    @Test
    void testFindActiveShippingMethods() {
        testShippingMethod.setActive(true);
        shippingMethodService.create(testShippingMethod);

        List<ShippingMethod> activeMethods = shippingMethodService.findActiveShippingMethods();
        assertNotNull(activeMethods);
        assertFalse(activeMethods.isEmpty());
        assertTrue(activeMethods.stream().allMatch(ShippingMethod::isActive));
    }

    @Test
    void testFindInactiveShippingMethods() {
        ShippingMethod inactive = ShippingMethod.builder()
                .name("Inactive Method")
                .description("Inactive shipping method")
                .cost(30.0)
                .estimatedDays(3)
                .isActive(false)
                .build();
        shippingMethodService.create(inactive);

        List<ShippingMethod> inactiveMethods = shippingMethodService.findInactiveShippingMethods();
        assertNotNull(inactiveMethods);
        assertTrue(inactiveMethods.stream().noneMatch(ShippingMethod::isActive));
    }

    @Test
    void testFindByCostRange() {
        shippingMethodService.create(testShippingMethod);

        ShippingMethod expensive = ShippingMethod.builder()
                .name("Express Shipping")
                .description("Express delivery in 1-2 business days")
                .cost(150.0)
                .estimatedDays(1)
                .isActive(true)
                .build();
        shippingMethodService.create(expensive);

        List<ShippingMethod> methods = shippingMethodService.findByCostRange(40.0, 100.0);
        assertNotNull(methods);
    }

    @Test
    void testFindActiveShippingMethodsOrderedByCost() {
        shippingMethodService.create(testShippingMethod);

        ShippingMethod expensive = ShippingMethod.builder()
                .name("Express Shipping")
                .description("Express delivery in 1-2 business days")
                .cost(150.0)
                .estimatedDays(1)
                .isActive(true)
                .build();
        shippingMethodService.create(expensive);

        List<ShippingMethod> methods = shippingMethodService.findActiveShippingMethodsOrderedByCost();
        assertNotNull(methods);
        assertFalse(methods.isEmpty());
    }

    @Test
    void testExistsByName() {
        shippingMethodService.create(testShippingMethod);
        assertTrue(shippingMethodService.existsByName("Standard Shipping"));
        assertFalse(shippingMethodService.existsByName("NonexistentMethod"));
    }

    @Test
    void testActivateShippingMethod() {
        ShippingMethod inactive = ShippingMethod.builder()
                .name("Inactive Method")
                .description("Inactive shipping method")
                .cost(30.0)
                .estimatedDays(3)
                .isActive(false)
                .build();
        ShippingMethod created = shippingMethodService.create(inactive);

        ShippingMethod activated = shippingMethodService.activateShippingMethod(created.getMethodId());
        assertNotNull(activated);
        assertTrue(activated.isActive());
    }

    @Test
    void testDeactivateShippingMethod() {
        ShippingMethod created = shippingMethodService.create(testShippingMethod);

        ShippingMethod deactivated = shippingMethodService.deactivateShippingMethod(created.getMethodId());
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }

    @Test
    void testCreateMultipleShippingMethods() {
        ShippingMethod standard = shippingMethodService.create(testShippingMethod);

        ShippingMethod express = ShippingMethod.builder()
                .name("Express Shipping")
                .description("Express delivery in 1-2 business days")
                .cost(150.0)
                .estimatedDays(1)
                .isActive(true)
                .build();
        ShippingMethod created2 = shippingMethodService.create(express);

        assertNotNull(standard);
        assertNotNull(created2);

        List<ShippingMethod> methods = shippingMethodService.getAll();
        assertTrue(methods.size() >= 2);
    }

    @Test
    void testShippingMethodValidation() {
        ShippingMethod valid = ShippingMethod.builder()
                .name("Valid Method")
                .description("Valid shipping method")
                .cost(100.0)
                .estimatedDays(7)
                .isActive(true)
                .build();
        ShippingMethod created = shippingMethodService.create(valid);
        assertNotNull(created);
        assertEquals("Valid Method", created.getName());
    }
}
