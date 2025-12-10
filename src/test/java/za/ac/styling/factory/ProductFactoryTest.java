package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(1L)
                .name("Electronics")
                .description("Electronic devices")
                .isActive(true)
                .build();
    }

    @Test
    void createProduct_WithBasicInfo_ShouldCreateProduct() {
        // Arrange
        String name = "Laptop";
        String description = "High-performance laptop";
        double basePrice = 999.99;
        String sku = "LAP001";

        // Act
        Product product = ProductFactory.createProduct(name, description, basePrice, sku, testCategory);

        // Assert
        assertNotNull(product);
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(basePrice, product.getBasePrice());
        assertEquals("LAP001", product.getSku());
        assertEquals(testCategory, product.getCategory());
        assertEquals(0.0, product.getComparePrice());
        assertTrue(product.isActive());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        assertNotNull(product.getColours());
        assertNotNull(product.getImages());
    }

    @Test
    void createProduct_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("", "Description", 99.99, "SKU001", testCategory)
        );

        assertEquals("Product name cannot be empty", exception.getMessage());
    }

    @Test
    void createProduct_WithNullName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct(null, "Description", 99.99, "SKU001", testCategory)
        );

        assertEquals("Product name cannot be empty", exception.getMessage());
    }

    @Test
    void createProduct_WithNegativePrice_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("Product", "Description", -10.0, "SKU001", testCategory)
        );

        assertEquals("Invalid base price", exception.getMessage());
    }

    @Test
    void createProduct_WithInvalidSKU_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("Product", "Description", 99.99, "abc", testCategory)
        );

        assertEquals("Invalid SKU format", exception.getMessage());
    }

    @Test
    void createProduct_WithNullCategory_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("Product", "Description", 99.99, "SKU001", null)
        );

        assertEquals("Category is required", exception.getMessage());
    }

    @Test
    void createProduct_SKUShouldBeUppercase() {
        // Act
        Product product = ProductFactory.createProduct("Product", "Description", 99.99, 
                "sku001", testCategory);

        // Assert
        assertEquals("SKU001", product.getSku());
    }

    @Test
    void createProduct_WithCompleteInfo_ShouldCreateProduct() {
        // Arrange
        String name = "Smartphone";
        String description = "Latest smartphone";
        double basePrice = 699.99;
        double comparePrice = 799.99;
        String sku = "PHN001";
        double weight = 0.2;

        // Act
        Product product = ProductFactory.createProduct(name, description, basePrice, 
                comparePrice, sku, weight, testCategory);

        // Assert
        assertNotNull(product);
        assertEquals(name, product.getName());
        assertEquals(basePrice, product.getBasePrice());
        assertEquals(comparePrice, product.getComparePrice());
        assertEquals("PHN001", product.getSku());
        assertEquals(weight, product.getWeight());
    }

    @Test
    void createProduct_WithInvalidComparePrice_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("Product", "Description", 99.99, 
                        -50.0, "SKU001", 1.0, testCategory)
        );

        assertEquals("Invalid compare price", exception.getMessage());
    }

    @Test
    void createProduct_WithInvalidWeight_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct("Product", "Description", 99.99, 
                        149.99, "SKU001", 0.0, testCategory)
        );

        assertEquals("Invalid weight", exception.getMessage());
    }

    @Test
    void createProductWithImage_ShouldSetPrimaryImage() {
        // Arrange
        ProductImage image = ProductImage.builder()
                .imageId(1L)
                .imageUrl("https://example.com/image.jpg")
                .isPrimary(true)
                .build();

        // Act
        Product product = ProductFactory.createProductWithImage("Product", "Description", 
                99.99, "SKU001", testCategory, image);

        // Assert
        assertNotNull(product);
        assertEquals(image, product.getPrimaryImage());
    }

    @Test
    void createProductWithImage_WithNullImage_ShouldCreateProductWithoutImage() {
        // Act
        Product product = ProductFactory.createProductWithImage("Product", "Description", 
                99.99, "SKU001", testCategory, null);

        // Assert
        assertNotNull(product);
        assertNull(product.getPrimaryImage());
    }

    @Test
    void createInactiveProduct_ShouldCreateInactiveProduct() {
        // Act
        Product product = ProductFactory.createInactiveProduct("Product", "Description", 
                99.99, "SKU001", testCategory);

        // Assert
        assertNotNull(product);
        assertFalse(product.isActive());
    }
}
