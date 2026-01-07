package za.ac.styling.service;

import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

import java.util.List;
import java.util.Optional;

public interface ProductColourSizeService extends IService<ProductColourSize, Integer> {

    List<ProductColourSize> findByColour(ProductColour colour);

    List<ProductColourSize> findByColourId(Integer colourId);

    Optional<ProductColourSize> findByColourAndSizeName(ProductColour colour, String sizeName);

    List<ProductColourSize> findLowStockItems();

    List<ProductColourSize> findOutOfStockItems();

    List<ProductColourSize> findAvailableSizesByColour(ProductColour colour);

    ProductColourSize reserveStock(Integer sizeId, int quantity);

    ProductColourSize releaseStock(Integer sizeId, int quantity);

    ProductColourSize completeSale(Integer sizeId, int quantity);

    ProductColourSize addStock(Integer sizeId, int quantity);

    boolean needsReordering(Integer sizeId);
}