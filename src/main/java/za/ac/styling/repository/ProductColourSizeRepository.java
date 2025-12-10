package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductColourSize entity
 */
@Repository
public interface ProductColourSizeRepository extends JpaRepository<ProductColourSize, Integer> {

    /**
     * Find all sizes for a colour
     */
    List<ProductColourSize> findByColour(ProductColour colour);

    /**
     * Find all sizes for a colour by colour ID
     */
    List<ProductColourSize> findByColourColourId(Integer colourId);

    /**
     * Find size by name and colour
     */
    Optional<ProductColourSize> findByColourAndSizeName(ProductColour colour, String sizeName);

    /**
     * Find sizes with low stock (at or below reorder level)
     */
    @Query("SELECT s FROM ProductColourSize s WHERE (s.stockQuantity - s.reservedQuantity) <= s.reorderLevel")
    List<ProductColourSize> findLowStockItems();

    /**
     * Find sizes out of stock
     */
    @Query("SELECT s FROM ProductColourSize s WHERE (s.stockQuantity - s.reservedQuantity) <= 0")
    List<ProductColourSize> findOutOfStockItems();

    /**
     * Find available sizes for a colour (with stock)
     */
    @Query("SELECT s FROM ProductColourSize s WHERE s.colour = ?1 AND (s.stockQuantity - s.reservedQuantity) > 0")
    List<ProductColourSize> findAvailableSizesByColour(ProductColour colour);
}
