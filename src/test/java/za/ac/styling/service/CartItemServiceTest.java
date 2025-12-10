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
class CartItemServiceTest {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RoleService roleService;

    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        Role role = roleService.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleService.create(RoleFactory.createRole("CUSTOMER")));

        User user = UserFactory.createUser(
                "cartitemuser",
                "cartitem@example.com",
                "password123",
                "CartItem",
                "User",
                "6667778888",
                role
        );
        user = userService.create(user);

        testCart = CartFactory.createCart(user);
        testCart = cartService.create(testCart);

        Category category = CategoryFactory.createCategory("Electronics", "Devices");
        category = categoryService.create(category);

        testProduct = ProductFactory.createProduct(
                "Mouse",
                "Wireless Mouse",
                29.99,
                "SKU-MOUSE-CART",
                category
        );
        testProduct = productService.create(testProduct);

        ProductColour colour = ProductColourFactory.createProductColour("Black", "#000000", testProduct);
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 100, 10, colour);

        testCartItem = CartItemFactory.createCartItem(testCart, testProduct, colour, size, 2);
    }

    @Test
    void testCreate() {
        CartItem created = cartItemService.create(testCartItem);
        assertNotNull(created);
        assertNotNull(created.getCartItemId());
        assertEquals(2, created.getQuantity());
    }

    @Test
    void testRead() {
        CartItem created = cartItemService.create(testCartItem);
        CartItem found = cartItemService.read(created.getCartItemId());
        assertNotNull(found);
        assertEquals(created.getCartItemId(), found.getCartItemId());
    }

    @Test
    void testUpdate() {
        CartItem created = cartItemService.create(testCartItem);
        created.setQuantity(5);
        CartItem updated = cartItemService.update(created);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testGetAll() {
        cartItemService.create(testCartItem);
        List<CartItem> cartItems = cartItemService.getAll();
        assertNotNull(cartItems);
        assertFalse(cartItems.isEmpty());
    }

    @Test
    void testFindByCart() {
        cartItemService.create(testCartItem);
        List<CartItem> items = cartItemService.findByCart(testCart);
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    void testAddToCart() {
        CartItem added = cartItemService.addToCart(testCart, testProduct, 3);
        assertNotNull(added);
        assertEquals(3, added.getQuantity());
    }

    @Test
    void testAddToCartExistingProduct() {
        cartItemService.addToCart(testCart, testProduct, 2);
        CartItem updated = cartItemService.addToCart(testCart, testProduct, 3);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testUpdateQuantity() {
        CartItem created = cartItemService.create(testCartItem);
        CartItem updated = cartItemService.updateQuantity(created.getCartItemId(), 10);
        assertNotNull(updated);
        assertEquals(10, updated.getQuantity());
    }

    @Test
    void testRemoveFromCart() {
        CartItem created = cartItemService.create(testCartItem);
        cartItemService.removeFromCart(created.getCartItemId());
        CartItem found = cartItemService.read(created.getCartItemId());
        assertNull(found);
    }

    @Test
    void testCountByCart() {
        cartItemService.create(testCartItem);
        long count = cartItemService.countByCart(testCart);
        assertTrue(count >= 1);
    }
}
