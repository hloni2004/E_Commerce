package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.factory.AddressFactory;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.UserFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        Role role = RoleFactory.createRole("CUSTOMER");
        role = roleService.create(role);

        testUser = UserFactory.createUser(
                "addressuser",
                "address@example.com",
                "password123",
                "Address",
                "User",
                "5556667777",
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
    }

    @Test
    void testCreate() {
        Address created = addressService.create(testAddress);
        assertNotNull(created);
        assertNotNull(created.getAddressId());
        assertEquals("123 Main St", created.getStreet());
    }

    @Test
    void testRead() {
        Address created = addressService.create(testAddress);
        Address found = addressService.read(created.getAddressId());
        assertNotNull(found);
        assertEquals(created.getAddressId(), found.getAddressId());
    }

    @Test
    void testUpdate() {
        Address created = addressService.create(testAddress);
        created.setCity("Los Angeles");
        Address updated = addressService.update(created);
        assertNotNull(updated);
        assertEquals("Los Angeles", updated.getCity());
    }

    @Test
    void testGetAll() {
        addressService.create(testAddress);
        List<Address> addresses = addressService.getAll();
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty());
    }

    @Test
    void testFindByUser() {
        addressService.create(testAddress);
        List<Address> addresses = addressService.findByUser(testUser);
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty());
    }

    @Test
    void testFindByUserAndAddressType() {
        addressService.create(testAddress);
        List<Address> addresses = addressService.findByUserAndAddressType(testUser, AddressType.SHIPPING);
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty());
    }

    @Test
    void testSetAsDefault() {
        Address created = addressService.create(testAddress);
        Address defaultAddress = addressService.setAsDefault(created.getAddressId());
        assertNotNull(defaultAddress);
        assertTrue(defaultAddress.isDefault());
    }

    @Test
    void testFindDefaultAddressByUser() {
        Address created = addressService.create(testAddress);
        addressService.setAsDefault(created.getAddressId());
        Optional<Address> found = addressService.findDefaultAddressByUser(testUser);
        assertTrue(found.isPresent());
        assertTrue(found.get().isDefault());
    }
}
