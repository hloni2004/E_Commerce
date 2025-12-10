package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class CartFactoryTest {

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
    void createCart_WithValidUser_ShouldCreateCart() {
        // Act
        Cart cart = CartFactory.createCart(testUser);

        // Assert
        assertNotNull(cart);
        assertEquals(testUser, cart.getUser());
        assertNotNull(cart.getItems());
        assertTrue(cart.getItems().isEmpty());
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
    }

    @Test
    void createCart_WithNullUser_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartFactory.createCart(null)
        );

        assertEquals("User is required", exception.getMessage());
    }

    @Test
    void createGuestCart_ShouldCreateCartWithoutUser() {
        // Act
        Cart cart = CartFactory.createGuestCart();

        // Assert
        assertNotNull(cart);
        assertNull(cart.getUser());
        assertNotNull(cart.getItems());
        assertTrue(cart.getItems().isEmpty());
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
    }

    @Test
    void createCartWithUserAssociation_ShouldAssociateCartWithUser() {
        // Act
        Cart cart = CartFactory.createCartWithUserAssociation(testUser);

        // Assert
        assertNotNull(cart);
        assertEquals(testUser, cart.getUser());
        assertEquals(cart, testUser.getCart());
    }

    @Test
    void createCartWithUserAssociation_WithNullUser_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartFactory.createCartWithUserAssociation(null)
        );

        assertEquals("User is required", exception.getMessage());
    }
}
