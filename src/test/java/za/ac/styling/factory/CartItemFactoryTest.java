package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.*;

import static org.junit.jupiter.api.Assertions.*;

class CartItemFactoryTest {

    private Cart testCart;
    private Product testProduct;
    private ProductColour testColour;
    private ProductColourSize testSize;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userId(1)
                .username("testuser")
                .build();

        testCart = Cart.builder()
                .cartId(1)
                .user(user)
                .build();

        Category category = Category.builder()
                .categoryId(1L)
                .name("Clothing")
                .build();

        testProduct = Product.builder()
                .productId(1)
                .name("T-Shirt")
                .basePrice(29.99)
                .category(category)
                .build();

        testColour = ProductColour.builder()
                .colourId(1)
                .name("Blue")
                .hexCode("#0000FF")
                .product(testProduct)
                .build();

        testSize = ProductColourSize.builder()
                .sizeId(1)
                .sizeName("M")
                .stockQuantity(50)
                .reservedQuantity(0)
                .colour(testColour)
                .build();
    }

    @Test
    void createCartItem_WithValidData_ShouldCreateCartItem() {
        // Act
        CartItem cartItem = CartItemFactory.createCartItem(testCart, testProduct, 
                testColour, testSize, 2);

        // Assert
        assertNotNull(cartItem);
        assertEquals(testCart, cartItem.getCart());
        assertEquals(testProduct, cartItem.getProduct());
        assertEquals(testColour, cartItem.getColour());
        assertEquals(testSize, cartItem.getSize());
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void createCartItem_WithNullCart_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(null, testProduct, testColour, testSize, 1)
        );

        assertEquals("Cart is required", exception.getMessage());
    }

    @Test
    void createCartItem_WithNullProduct_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, null, testColour, testSize, 1)
        );

        assertEquals("Product is required", exception.getMessage());
    }

    @Test
    void createCartItem_WithNullColour_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, testProduct, null, testSize, 1)
        );

        assertEquals("Product colour is required", exception.getMessage());
    }

    @Test
    void createCartItem_WithNullSize_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, testProduct, testColour, null, 1)
        );

        assertEquals("Product size is required", exception.getMessage());
    }

    @Test
    void createCartItem_WithZeroQuantity_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, testProduct, testColour, testSize, 0)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void createCartItem_WithNegativeQuantity_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, testProduct, testColour, testSize, -1)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void createCartItem_WithQuantityExceedingStock_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.createCartItem(testCart, testProduct, testColour, testSize, 100)
        );

        assertEquals("Quantity exceeds available stock", exception.getMessage());
    }

    @Test
    void createCartItem_WithDefaultQuantity_ShouldCreateWithQuantityOne() {
        // Act
        CartItem cartItem = CartItemFactory.createCartItem(testCart, testProduct, 
                testColour, testSize);

        // Assert
        assertNotNull(cartItem);
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    void updateQuantity_WithValidQuantity_ShouldUpdateQuantity() {
        // Arrange
        CartItem cartItem = CartItemFactory.createCartItem(testCart, testProduct, 
                testColour, testSize, 2);

        // Act
        CartItem updated = CartItemFactory.updateQuantity(cartItem, 5);

        // Assert
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void updateQuantity_WithNullCartItem_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.updateQuantity(null, 5)
        );

        assertEquals("CartItem cannot be null", exception.getMessage());
    }

    @Test
    void updateQuantity_WithZeroQuantity_ShouldThrowException() {
        // Arrange
        CartItem cartItem = CartItemFactory.createCartItem(testCart, testProduct, 
                testColour, testSize, 2);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.updateQuantity(cartItem, 0)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void updateQuantity_ExceedingStock_ShouldThrowException() {
        // Arrange
        CartItem cartItem = CartItemFactory.createCartItem(testCart, testProduct, 
                testColour, testSize, 2);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CartItemFactory.updateQuantity(cartItem, 100)
        );

        assertEquals("Quantity exceeds available stock", exception.getMessage());
    }
}
