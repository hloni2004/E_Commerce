package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import static org.junit.jupiter.api.Assertions.*;

class ProductImageFactoryTest {

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .categoryId(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .productId(1)
                .name("Laptop")
                .basePrice(999.99)
                .category(category)
                .build();
    }

    @Test
    void createProductImage_WithAllInfo_ShouldCreateImage() {
        // Arrange
        String imageUrl = "https://example.com/laptop.jpg";
        String altText = "High-performance laptop";
        int displayOrder = 1;

        // Act
        ProductImage image = ProductImageFactory.createProductImage(testProduct, imageUrl, 
                altText, displayOrder, false);

        // Assert
        assertNotNull(image);
        assertEquals(testProduct, image.getProduct());
        assertEquals(imageUrl, image.getImageUrl());
        assertEquals(altText, image.getAltText());
        assertEquals(displayOrder, image.getDisplayOrder());
        assertFalse(image.isPrimary());
    }

    @Test
    void createProductImage_WithNullProduct_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.createProductImage(null, "https://example.com/image.jpg", 
                        "Alt text", 0, false)
        );

        assertEquals("Product is required", exception.getMessage());
    }

    @Test
    void createProductImage_WithEmptyUrl_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.createProductImage(testProduct, "", "Alt text", 0, false)
        );

        assertEquals("Image URL cannot be empty", exception.getMessage());
    }

    @Test
    void createProductImage_WithNegativeDisplayOrder_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.createProductImage(testProduct, "https://example.com/image.jpg", 
                        "Alt text", -1, false)
        );

        assertEquals("Display order cannot be negative", exception.getMessage());
    }

    @Test
    void createProductImage_WithNullAltText_ShouldUseProductName() {
        // Act
        ProductImage image = ProductImageFactory.createProductImage(testProduct, 
                "https://example.com/laptop.jpg", null, 0, false);

        // Assert
        assertEquals(testProduct.getName(), image.getAltText());
    }

    @Test
    void createProductImage_WithDefaultValues_ShouldCreateImage() {
        // Act
        ProductImage image = ProductImageFactory.createProductImage(testProduct, 
                "https://example.com/laptop.jpg");

        // Assert
        assertNotNull(image);
        assertEquals(0, image.getDisplayOrder());
        assertFalse(image.isPrimary());
    }

    @Test
    void createPrimaryProductImage_ShouldCreatePrimaryImage() {
        // Act
        ProductImage image = ProductImageFactory.createPrimaryProductImage(testProduct, 
                "https://example.com/laptop.jpg", "Primary laptop image");

        // Assert
        assertTrue(image.isPrimary());
        assertEquals(0, image.getDisplayOrder());
    }

    @Test
    void createSecondaryProductImage_ShouldCreateSecondaryImage() {
        // Act
        ProductImage image = ProductImageFactory.createSecondaryProductImage(testProduct, 
                "https://example.com/laptop-2.jpg", "Secondary laptop image", 2);

        // Assert
        assertFalse(image.isPrimary());
        assertEquals(2, image.getDisplayOrder());
    }

    @Test
    void updateDisplayOrder_ShouldUpdateOrder() {
        // Arrange
        ProductImage image = ProductImageFactory.createProductImage(testProduct, 
                "https://example.com/laptop.jpg");

        // Act
        ProductImage updated = ProductImageFactory.updateDisplayOrder(image, 5);

        // Assert
        assertEquals(5, updated.getDisplayOrder());
    }

    @Test
    void updateDisplayOrder_WithNullImage_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.updateDisplayOrder(null, 5)
        );

        assertEquals("ProductImage cannot be null", exception.getMessage());
    }

    @Test
    void updateDisplayOrder_WithNegativeOrder_ShouldThrowException() {
        // Arrange
        ProductImage image = ProductImageFactory.createProductImage(testProduct, 
                "https://example.com/laptop.jpg");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.updateDisplayOrder(image, -1)
        );

        assertEquals("Display order cannot be negative", exception.getMessage());
    }

    @Test
    void setAsPrimary_ShouldSetImageAsPrimary() {
        // Arrange
        ProductImage image = ProductImageFactory.createProductImage(testProduct, 
                "https://example.com/laptop.jpg", "Alt text", 3, false);

        // Act
        ProductImage updated = ProductImageFactory.setAsPrimary(image);

        // Assert
        assertTrue(updated.isPrimary());
        assertEquals(0, updated.getDisplayOrder());
    }

    @Test
    void setAsPrimary_WithNullImage_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.setAsPrimary(null)
        );

        assertEquals("ProductImage cannot be null", exception.getMessage());
    }

    @Test
    void setAsSecondary_ShouldSetImageAsSecondary() {
        // Arrange
        ProductImage image = ProductImageFactory.createPrimaryProductImage(testProduct, 
                "https://example.com/laptop.jpg", "Alt text");

        // Act
        ProductImage updated = ProductImageFactory.setAsSecondary(image, 2);

        // Assert
        assertFalse(updated.isPrimary());
        assertEquals(2, updated.getDisplayOrder());
    }

    @Test
    void setAsSecondary_WithNullImage_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductImageFactory.setAsSecondary(null, 2)
        );

        assertEquals("ProductImage cannot be null", exception.getMessage());
    }
}
