package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Address;
import za.ac.styling.domain.AddressType;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class AddressFactoryTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        Role customerRole = Role.builder()
                .roleId(1)
                .roleName("CUSTOMER")
                .build();

        testUser = User.builder()
                .userId(1)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(customerRole)
                .build();
    }

    @Test
    void createAddress_WithValidData_ShouldCreateAddress() {
        // Arrange
        String street = "123 Main Street";
        String city = "Johannesburg";
        String state = "Gauteng";
        String zipCode = "2000";
        String country = "South Africa";

        // Act
        Address address = AddressFactory.createAddress(street, city, state, zipCode, country,
                AddressType.SHIPPING, testUser);

        // Assert
        assertNotNull(address);
        assertEquals(street, address.getAddressLine1());
        assertEquals(city, address.getCity());
        assertEquals(state, address.getProvince());
        assertEquals(zipCode, address.getPostalCode());
        assertEquals(country, address.getCountry());
        assertEquals(AddressType.SHIPPING, address.getAddressType());
        assertEquals(testUser, address.getUser());
        assertFalse(address.isDefault());
    }

    @Test
    void createAddress_WithEmptyStreet_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("", "City", "State", "2000", "Country",
                        AddressType.SHIPPING, testUser)
        );

        assertEquals("Street address cannot be empty", exception.getMessage());
    }

    @Test
    void createAddress_WithEmptyCity_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "", "State", "2000", "Country",
                        AddressType.SHIPPING, testUser)
        );

        assertEquals("City cannot be empty", exception.getMessage());
    }

    @Test
    void createAddress_WithEmptyState_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "City", "", "2000", "Country",
                        AddressType.SHIPPING, testUser)
        );

        assertEquals("State cannot be empty", exception.getMessage());
    }

    @Test
    void createAddress_WithInvalidZipCode_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "City", "State", "ABC", "Country",
                        AddressType.SHIPPING, testUser)
        );

        assertEquals("Invalid zip code format", exception.getMessage());
    }

    @Test
    void createAddress_WithEmptyCountry_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "City", "State", "2000", "",
                        AddressType.SHIPPING, testUser)
        );

        assertEquals("Country cannot be empty", exception.getMessage());
    }

    @Test
    void createAddress_WithNullAddressType_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "City", "State", "2000", "Country",
                        null, testUser)
        );

        assertEquals("Address type is required", exception.getMessage());
    }

    @Test
    void createAddress_WithNullUser_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AddressFactory.createAddress("Street", "City", "State", "2000", "Country",
                        AddressType.SHIPPING, null)
        );

        assertEquals("User is required", exception.getMessage());
    }

    @Test
    void createDefaultAddress_ShouldSetDefaultToTrue() {
        // Act
        Address address = AddressFactory.createDefaultAddress("Street", "City", "State",
                "2000", "Country", AddressType.SHIPPING, testUser);

        // Assert
        assertNotNull(address);
        assertTrue(address.isDefault());
    }

    @Test
    void createShippingAddress_ShouldCreateShippingType() {
        // Act
        Address address = AddressFactory.createShippingAddress("Street", "City", "State",
                "2000", "Country", testUser);

        // Assert
        assertNotNull(address);
        assertEquals(AddressType.SHIPPING, address.getAddressType());
    }

    @Test
    void createBillingAddress_ShouldCreateBillingType() {
        // Act
        Address address = AddressFactory.createBillingAddress("Street", "City", "State",
                "2000", "Country", testUser);

        // Assert
        assertNotNull(address);
        assertEquals(AddressType.BILLING, address.getAddressType());
    }

    @Test
    void createBothAddress_ShouldCreateBothType() {
        // Act
        Address address = AddressFactory.createBothAddress("Street", "City", "State",
                "2000", "Country", testUser);

        // Assert
        assertNotNull(address);
        assertEquals(AddressType.BOTH, address.getAddressType());
    }
}
