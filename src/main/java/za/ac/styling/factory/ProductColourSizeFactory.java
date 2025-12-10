package za.ac.styling.factory;

import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating ProductColourSize objects
 */
public class ProductColourSizeFactory {

    /**
     * Creates a new ProductColourSize with all information
     */
    public static ProductColourSize createProductColourSize(String sizeName, int stockQuantity,
                                                           int reorderLevel, ProductColour colour) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(sizeName)) {
            throw new IllegalArgumentException("Size name cannot be empty");
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }

        if (colour == null) {
            throw new IllegalArgumentException("Product colour is required");
        }

        return ProductColourSize.builder()
                .sizeName(sizeName.trim().toUpperCase())
                .stockQuantity(stockQuantity)
                .reservedQuantity(0)
                .reorderLevel(reorderLevel)
                .colour(colour)
                .build();
    }

    /**
     * Creates a new ProductColourSize with default reorder level
     */
    public static ProductColourSize createProductColourSize(String sizeName, int stockQuantity,
                                                           ProductColour colour) {
        int defaultReorderLevel = Math.max(5, stockQuantity / 10);
        return createProductColourSize(sizeName, stockQuantity, defaultReorderLevel, colour);
    }

    /**
     * Creates an XS size variant
     */
    public static ProductColourSize createXSSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("XS", stockQuantity, colour);
    }

    /**
     * Creates a S size variant
     */
    public static ProductColourSize createSmallSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("S", stockQuantity, colour);
    }

    /**
     * Creates a M size variant
     */
    public static ProductColourSize createMediumSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("M", stockQuantity, colour);
    }

    /**
     * Creates a L size variant
     */
    public static ProductColourSize createLargeSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("L", stockQuantity, colour);
    }

    /**
     * Creates an XL size variant
     */
    public static ProductColourSize createXLSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("XL", stockQuantity, colour);
    }

    /**
     * Creates an XXL size variant
     */
    public static ProductColourSize createXXLSize(int stockQuantity, ProductColour colour) {
        return createProductColourSize("XXL", stockQuantity, colour);
    }

    /**
     * Reserves stock for a size
     */
    public static ProductColourSize reserveStock(ProductColourSize size, int quantity) {
        if (size == null) {
            throw new IllegalArgumentException("ProductColourSize cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        int availableStock = size.getStockQuantity() - size.getReservedQuantity();
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Insufficient stock available");
        }

        size.setReservedQuantity(size.getReservedQuantity() + quantity);
        return size;
    }

    /**
     * Releases reserved stock for a size
     */
    public static ProductColourSize releaseStock(ProductColourSize size, int quantity) {
        if (size == null) {
            throw new IllegalArgumentException("ProductColourSize cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (quantity > size.getReservedQuantity()) {
            throw new IllegalArgumentException("Cannot release more than reserved quantity");
        }

        size.setReservedQuantity(size.getReservedQuantity() - quantity);
        return size;
    }

    /**
     * Completes a sale by reducing stock quantity and reserved quantity
     */
    public static ProductColourSize completeSale(ProductColourSize size, int quantity) {
        if (size == null) {
            throw new IllegalArgumentException("ProductColourSize cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (quantity > size.getReservedQuantity()) {
            throw new IllegalArgumentException("Quantity exceeds reserved stock");
        }

        size.setStockQuantity(size.getStockQuantity() - quantity);
        size.setReservedQuantity(size.getReservedQuantity() - quantity);
        return size;
    }

    /**
     * Adds stock to a size
     */
    public static ProductColourSize addStock(ProductColourSize size, int quantity) {
        if (size == null) {
            throw new IllegalArgumentException("ProductColourSize cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        size.setStockQuantity(size.getStockQuantity() + quantity);
        return size;
    }

    /**
     * Checks if size needs reordering
     */
    public static boolean needsReordering(ProductColourSize size) {
        if (size == null) {
            return false;
        }
        int availableStock = size.getStockQuantity() - size.getReservedQuantity();
        return availableStock <= size.getReorderLevel();
    }
}
