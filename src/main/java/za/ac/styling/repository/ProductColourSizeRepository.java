package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductColourSizeRepository extends JpaRepository<ProductColourSize, Integer> {

    List<ProductColourSize> findByColour(ProductColour colour);

    List<ProductColourSize> findByColourColourId(Integer colourId);

    Optional<ProductColourSize> findByColourAndSizeName(ProductColour colour, String sizeName);

    @Query("SELECT s FROM ProductColourSize s WHERE (s.stockQuantity - s.reservedQuantity) <= s.reorderLevel")
    List<ProductColourSize> findLowStockItems();

    @Query("SELECT s FROM ProductColourSize s WHERE (s.stockQuantity - s.reservedQuantity) <= 0")
    List<ProductColourSize> findOutOfStockItems();

    @Query("SELECT s FROM ProductColourSize s WHERE s.colour = ?1 AND (s.stockQuantity - s.reservedQuantity) > 0")
    List<ProductColourSize> findAvailableSizesByColour(ProductColour colour);
}
