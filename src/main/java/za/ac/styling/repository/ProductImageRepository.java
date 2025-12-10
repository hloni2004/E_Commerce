package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductImage entity
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Find all images for a product
     */
    List<ProductImage> findByProduct(Product product);

    /**
     * Find all images for a product by product ID
     */
    List<ProductImage> findByProductProductId(Integer productId);

    /**
     * Find images ordered by display order
     */
    List<ProductImage> findByProductOrderByDisplayOrderAsc(Product product);

    /**
     * Find primary image for a product
     */
    Optional<ProductImage> findByProductAndIsPrimaryTrue(Product product);

    /**
     * Find secondary images for a product
     */
    List<ProductImage> findByProductAndIsPrimaryFalse(Product product);
}
