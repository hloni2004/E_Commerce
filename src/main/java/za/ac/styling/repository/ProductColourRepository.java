package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;

import java.util.List;

/**
 * Repository interface for ProductColour entity
 */
@Repository
public interface ProductColourRepository extends JpaRepository<ProductColour, Integer> {

    /**
     * Find all colours for a product
     */
    List<ProductColour> findByProduct(Product product);

    /**
     * Find all colours for a product by product ID
     */
    List<ProductColour> findByProductProductId(Integer productId);

    /**
     * Find colour by name and product
     */
    List<ProductColour> findByProductAndName(Product product, String name);
}
