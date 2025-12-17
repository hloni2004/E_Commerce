package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for ProductImage entity
 */
public interface ProductImageService extends IService<ProductImage, Long> {

    /**
     * Find all images for a product
     */
    List<ProductImage> findByProduct(Product product);

    /**
     * Find all images for a product by product ID
     */
    List<ProductImage> findByProductId(Integer productId);

    /**
     * Find images ordered by display order
     */
    List<ProductImage> findByProductOrderByDisplayOrder(Product product);

    /**
     * Find primary image for a product
     */
    Optional<ProductImage> findPrimaryImageByProduct(Product product);

    /**
     * Find secondary images for a product
     */
    List<ProductImage> findSecondaryImagesByProduct(Product product);

    /**
     * Set image as primary
     */
    ProductImage setAsPrimary(Long imageId);

    /**
     * Update display order
     */
    ProductImage updateDisplayOrder(Long imageId, int newDisplayOrder);
    
    /**
     * Get maximum display order for a product
     */
    int getMaxDisplayOrder(Integer productId);
}