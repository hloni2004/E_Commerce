package za.ac.styling.service;

import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for ProductColourSize entity
 */
public interface ProductColourSizeService extends IService<ProductColourSize, Integer> {

    /**
     * Find all sizes for a colour
     */
    List<ProductColourSize> findByColour(ProductColour colour);

    /**
     * Find all sizes for a colour by colour ID
     */
    List<ProductColourSize> findByColourId(Integer colourId);

    /**
     * Find size by name and colour
     */
    Optional<ProductColourSize> findByColourAndSizeName(ProductColour colour, String sizeName);

    /**
     * Find sizes with low stock (at or below reorder level)
     */
    List<ProductColourSize> findLowStockItems();

    /**
     * Find sizes out of stock
     */
    List<ProductColourSize> findOutOfStockItems();

    /**
     * Find available sizes for a colour (with stock)
     */
    List<ProductColourSize> findAvailableSizesByColour(ProductColour colour);

    /**
     * Reserve stock for a size
     */
    ProductColourSize reserveStock(Integer sizeId, int quantity);

    /**
     * Release reserved stock for a size
     */
    ProductColourSize releaseStock(Integer sizeId, int quantity);

    /**
     * Complete sale by reducing stock and reserved quantity
     */
    ProductColourSize completeSale(Integer sizeId, int quantity);

    /**
     * Add stock to a size
     */
    ProductColourSize addStock(Integer sizeId, int quantity);

    /**
     * Check if size needs reordering
     */
    boolean needsReordering(Integer sizeId);
}