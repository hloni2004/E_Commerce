package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

import static org.junit.jupiter.api.Assertions.*;

class ProductColourSizeFactoryTest {

    private ProductColour testColour;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .categoryId(1L)
                .name("Clothing")
                .build();

        Product product = Product.builder()
                .productId(1)
                .name("T-Shirt")
                .basePrice(29.99)
                .category(category)
                .build();

        testColour = ProductColour.builder()
                .colourId(1)
                .name("Blue")
                .hexCode("#0000FF")
                .product(product)
                .build();
    }

    @Test
    void createProductColourSize_WithValidData_ShouldCreateSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, 10, testColour);

        // Assert
        assertNotNull(size);
        assertEquals("M", size.getSizeName());
        assertEquals(100, size.getStockQuantity());
        assertEquals(0, size.getReservedQuantity());
        assertEquals(10, size.getReorderLevel());
        assertEquals(testColour, size.getColour());
    }

    @Test
    void createProductColourSize_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.createProductColourSize("", 100, 10, testColour)
        );

        assertEquals("Size name cannot be empty", exception.getMessage());
    }

    @Test
    void createProductColourSize_WithNegativeStock_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.createProductColourSize("M", -10, 5, testColour)
        );

        assertEquals("Stock quantity cannot be negative", exception.getMessage());
    }

    @Test
    void createProductColourSize_WithNegativeReorderLevel_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.createProductColourSize("M", 100, -5, testColour)
        );

        assertEquals("Reorder level cannot be negative", exception.getMessage());
    }

    @Test
    void createProductColourSize_WithNullColour_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.createProductColourSize("M", 100, 10, null)
        );

        assertEquals("Product colour is required", exception.getMessage());
    }

    @Test
    void createProductColourSize_SizeNameShouldBeUppercase() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("small", 
                100, testColour);

        // Assert
        assertEquals("SMALL", size.getSizeName());
    }

    @Test
    void createProductColourSize_WithDefaultReorderLevel_ShouldCalculateReorderLevel() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);

        // Assert
        assertEquals(10, size.getReorderLevel()); // 100 / 10 = 10
    }

    @Test
    void createXSSize_ShouldCreateXSSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createXSSize(50, testColour);

        // Assert
        assertEquals("XS", size.getSizeName());
        assertEquals(50, size.getStockQuantity());
    }

    @Test
    void createSmallSize_ShouldCreateSmallSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createSmallSize(75, testColour);

        // Assert
        assertEquals("S", size.getSizeName());
    }

    @Test
    void createMediumSize_ShouldCreateMediumSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createMediumSize(100, testColour);

        // Assert
        assertEquals("M", size.getSizeName());
    }

    @Test
    void createLargeSize_ShouldCreateLargeSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createLargeSize(100, testColour);

        // Assert
        assertEquals("L", size.getSizeName());
    }

    @Test
    void createXLSize_ShouldCreateXLSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createXLSize(75, testColour);

        // Assert
        assertEquals("XL", size.getSizeName());
    }

    @Test
    void createXXLSize_ShouldCreateXXLSize() {
        // Act
        ProductColourSize size = ProductColourSizeFactory.createXXLSize(50, testColour);

        // Assert
        assertEquals("XXL", size.getSizeName());
    }

    @Test
    void reserveStock_WithValidQuantity_ShouldReserveStock() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);

        // Act
        ProductColourSize updated = ProductColourSizeFactory.reserveStock(size, 10);

        // Assert
        assertEquals(10, updated.getReservedQuantity());
    }

    @Test
    void reserveStock_ExceedingAvailableStock_ShouldThrowException() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.reserveStock(size, 150)
        );

        assertEquals("Insufficient stock available", exception.getMessage());
    }

    @Test
    void releaseStock_WithValidQuantity_ShouldReleaseStock() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);
        ProductColourSizeFactory.reserveStock(size, 20);

        // Act
        ProductColourSize updated = ProductColourSizeFactory.releaseStock(size, 10);

        // Assert
        assertEquals(10, updated.getReservedQuantity());
    }

    @Test
    void releaseStock_ExceedingReservedQuantity_ShouldThrowException() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);
        ProductColourSizeFactory.reserveStock(size, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.releaseStock(size, 20)
        );

        assertEquals("Cannot release more than reserved quantity", exception.getMessage());
    }

    @Test
    void completeSale_WithValidQuantity_ShouldReduceStockAndReserved() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);
        ProductColourSizeFactory.reserveStock(size, 10);

        // Act
        ProductColourSize updated = ProductColourSizeFactory.completeSale(size, 10);

        // Assert
        assertEquals(90, updated.getStockQuantity());
        assertEquals(0, updated.getReservedQuantity());
    }

    @Test
    void completeSale_ExceedingReservedStock_ShouldThrowException() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);
        ProductColourSizeFactory.reserveStock(size, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ProductColourSizeFactory.completeSale(size, 20)
        );

        assertEquals("Quantity exceeds reserved stock", exception.getMessage());
    }

    @Test
    void addStock_ShouldIncreaseStockQuantity() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, testColour);

        // Act
        ProductColourSize updated = ProductColourSizeFactory.addStock(size, 50);

        // Assert
        assertEquals(150, updated.getStockQuantity());
    }

    @Test
    void needsReordering_BelowReorderLevel_ShouldReturnTrue() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, 20, testColour);
        ProductColourSizeFactory.reserveStock(size, 90);

        // Act
        boolean needsReorder = ProductColourSizeFactory.needsReordering(size);

        // Assert
        assertTrue(needsReorder);
    }

    @Test
    void needsReordering_AboveReorderLevel_ShouldReturnFalse() {
        // Arrange
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 
                100, 10, testColour);

        // Act
        boolean needsReorder = ProductColourSizeFactory.needsReordering(size);

        // Assert
        assertFalse(needsReorder);
    }
}
