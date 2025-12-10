package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.factory.CartFactory;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.UserFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        Role role = roleService.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleService.create(RoleFactory.createRole("CUSTOMER")));

        testUser = UserFactory.createUser(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                "1112223333",
                role
        );
        testUser = userService.create(testUser);

        testCart = CartFactory.createCart(testUser);
    }

    @Test
    void testCreate() {
        Cart created = cartService.create(testCart);
        assertNotNull(created);
        assertNotNull(created.getCartId());
        assertEquals(testUser.getUserId(), created.getUser().getUserId());
    }

    @Test
    void testRead() {
        Cart created = cartService.create(testCart);
        Cart found = cartService.read(created.getCartId());
        assertNotNull(found);
        assertEquals(created.getCartId(), found.getCartId());
    }

    @Test
    void testUpdate() {
        Cart created = cartService.create(testCart);
        Cart updated = cartService.update(created);
        assertNotNull(updated);
    }

    @Test
    void testGetAll() {
        cartService.create(testCart);
        List<Cart> carts = cartService.getAll();
        assertNotNull(carts);
        assertFalse(carts.isEmpty());
    }

    @Test
    void testFindByUser() {
        cartService.create(testCart);
        Optional<Cart> found = cartService.findByUser(testUser);
        assertTrue(found.isPresent());
        assertEquals(testUser.getUserId(), found.get().getUser().getUserId());
    }

    @Test
    void testFindByUserId() {
        cartService.create(testCart);
        Optional<Cart> found = cartService.findByUserId(testUser.getUserId());
        assertTrue(found.isPresent());
    }

    @Test
    void testCreateCartForUser() {
        Cart created = cartService.createCartForUser(testUser);
        assertNotNull(created);
        assertNotNull(created.getCartId());
    }

    @Test
    void testCreateCartForUserTwice() {
        Cart first = cartService.createCartForUser(testUser);
        Cart second = cartService.createCartForUser(testUser);
        assertEquals(first.getCartId(), second.getCartId());
    }

    @Test
    void testClearCart() {
        Cart created = cartService.create(testCart);
        cartService.clearCart(created.getCartId());
        Cart found = cartService.read(created.getCartId());
        assertNotNull(found);
    }

    @Test
    void testGetCartTotal() {
        Cart created = cartService.create(testCart);
        double total = cartService.getCartTotal(created.getCartId());
        assertTrue(total >= 0.0);
    }
}
