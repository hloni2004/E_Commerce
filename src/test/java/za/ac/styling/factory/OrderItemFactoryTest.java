package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.*;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemFactoryTest {

    private Order testOrder;
    private Product testProduct;
    private ProductColour testColour;
    private ProductColourSize testSize;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userId(1)
                .username("testuser")
                .build();

        testOrder = Order.builder()
                .orderId(1)
                .user(user)
                .orderNumber("ORD001")
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
                .product(testProduct)
                .build();

        testSize = ProductColourSize.builder()
                .sizeId(1)
                .sizeName("M")
                .stockQuantity(50)
                .colour(testColour)
                .build();

        Cart cart = Cart.builder().cartId(1).user(user).build();

        testCartItem = CartItem.builder()
                .cartItemId(1)
                .cart(cart)
                .product(testProduct)
                .colour(testColour)
                .size(testSize)
                .quantity(2)
                .build();
    }

    @Test
    void createOrderItem_WithValidData_ShouldCreateOrderItem() {
        // Act
        OrderItem orderItem = OrderItemFactory.createOrderItem(testOrder, testProduct, 
                testColour, testSize, 2, 29.99);

        // Assert
        assertNotNull(orderItem);
        assertEquals(testOrder, orderItem.getOrder());
        assertEquals(testProduct, orderItem.getProduct());
        assertEquals(testColour, orderItem.getColour());
        assertEquals(testSize, orderItem.getColourSize());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(29.99, orderItem.getPrice());
        assertEquals(59.98, orderItem.getTotalPrice(), 0.01);
    }

    @Test
    void createOrderItem_WithNullOrder_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(null, testProduct, testColour, testSize, 2, 29.99)
        );

        assertEquals("Order is required", exception.getMessage());
    }

    @Test
    void createOrderItem_WithNullProduct_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(testOrder, null, testColour, testSize, 2, 29.99)
        );

        assertEquals("Product is required", exception.getMessage());
    }

    @Test
    void createOrderItem_WithZeroQuantity_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(testOrder, testProduct, testColour, testSize, 0, 29.99)
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void createOrderItem_WithNegativePrice_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(testOrder, testProduct, testColour, testSize, 2, -10.0)
        );

        assertEquals("Unit price cannot be negative", exception.getMessage());
    }

    @Test
    void createOrderItem_UsingProductPrice_ShouldUseBasePrice() {
        // Act
        OrderItem orderItem = OrderItemFactory.createOrderItem(testOrder, testProduct, 
                testColour, testSize, 2);

        // Assert
        assertEquals(testProduct.getBasePrice(), orderItem.getPrice());
    }

    @Test
    void createFromCartItem_ShouldCreateOrderItemFromCart() {
        // Act
        OrderItem orderItem = OrderItemFactory.createFromCartItem(testOrder, testCartItem);

        // Assert
        assertNotNull(orderItem);
        assertEquals(testOrder, orderItem.getOrder());
        assertEquals(testCartItem.getProduct(), orderItem.getProduct());
        assertEquals(testCartItem.getColour(), orderItem.getColour());
        assertEquals(testCartItem.getSize(), orderItem.getColourSize());
        assertEquals(testCartItem.getQuantity(), orderItem.getQuantity());
        assertEquals(testProduct.getBasePrice(), orderItem.getPrice());
    }

    @Test
    void createFromCartItem_WithNullCartItem_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createFromCartItem(testOrder, null)
        );

        assertEquals("CartItem cannot be null", exception.getMessage());
    }

    @Test
    void recalculateTotal_ShouldRecalculateTotalPrice() {
        // Arrange
        OrderItem orderItem = OrderItemFactory.createOrderItem(testOrder, testProduct, 
                testColour, testSize, 3, 25.00);

        // Act
        OrderItem recalculated = OrderItemFactory.recalculateTotal(orderItem);

        // Assert
        assertEquals(75.00, recalculated.getTotalPrice(), 0.01);
    }

    @Test
    void recalculateTotal_WithNullOrderItem_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.recalculateTotal(null)
        );

        assertEquals("OrderItem cannot be null", exception.getMessage());
    }
}
