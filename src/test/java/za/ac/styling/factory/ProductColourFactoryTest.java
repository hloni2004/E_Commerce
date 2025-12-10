package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;

import static org.junit.jupiter.api.Assertions.*;

class ProductColourFactoryTest {

    private Product testProduct;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void createProductColour_WithValidData_ShouldCreateColour() {
        // Act
        ProductColour colour = ProductColourFactory.createProductColour("Blue", "#0000FF", testProduct);

        // Assert
        assertNotNull(colour);
        assertEquals("Blue", colour.getName());
        assertEquals("#0000FF", colour.getHexCode());
        assertEquals(testProduct, colour.getProduct());
        assertNotNull(colour.getSizes());
        assertTrue(colour.getSizes().isEmpty());
    }

    @Test
    void createProductColour_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourFactory.createProductColour("", "#0000FF", testProduct)
        );

        assertEquals("Colour name cannot be empty", exception.getMessage());
    }

    @Test
    void createProductColour_WithInvalidHexCode_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourFactory.createProductColour("Blue", "0000FF", testProduct)
        );

        assertEquals("Invalid hex code format. Must be in format #RRGGBB", exception.getMessage());
    }

    @Test
    void createProductColour_WithNullProduct_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourFactory.createProductColour("Blue", "#0000FF", null)
        );

        assertEquals("Product is required", exception.getMessage());
    }

    @Test
    void createProductColour_HexCodeShouldBeUppercase() {
        // Act
        ProductColour colour = ProductColourFactory.createProductColour("Blue", "#0000ff", testProduct);

        // Assert
        assertEquals("#0000FF", colour.getHexCode());
    }

    @Test
    void createProductColour_WithoutHexCode_ShouldUseDefaultBlack() {
        // Act
        ProductColour colour = ProductColourFactory.createProductColour("Standard", testProduct);

        // Assert
        assertEquals("#000000", colour.getHexCode());
    }

    @Test
    void createBlackColour_ShouldCreateBlackVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createBlackColour(testProduct);

        // Assert
        assertEquals("Black", colour.getName());
        assertEquals("#000000", colour.getHexCode());
    }

    @Test
    void createWhiteColour_ShouldCreateWhiteVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createWhiteColour(testProduct);

        // Assert
        assertEquals("White", colour.getName());
        assertEquals("#FFFFFF", colour.getHexCode());
    }

    @Test
    void createRedColour_ShouldCreateRedVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createRedColour(testProduct);

        // Assert
        assertEquals("Red", colour.getName());
        assertEquals("#FF0000", colour.getHexCode());
    }

    @Test
    void createBlueColour_ShouldCreateBlueVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createBlueColour(testProduct);

        // Assert
        assertEquals("Blue", colour.getName());
        assertEquals("#0000FF", colour.getHexCode());
    }

    @Test
    void createGreenColour_ShouldCreateGreenVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createGreenColour(testProduct);

        // Assert
        assertEquals("Green", colour.getName());
        assertEquals("#00FF00", colour.getHexCode());
    }

    @Test
    void createGrayColour_ShouldCreateGrayVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createGrayColour(testProduct);

        // Assert
        assertEquals("Gray", colour.getName());
        assertEquals("#808080", colour.getHexCode());
    }

    @Test
    void createNavyColour_ShouldCreateNavyVariant() {
        // Act
        ProductColour colour = ProductColourFactory.createNavyColour(testProduct);

        // Assert
        assertEquals("Navy", colour.getName());
        assertEquals("#000080", colour.getHexCode());
    }
}
