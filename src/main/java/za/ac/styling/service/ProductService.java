package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product entity
 */
public interface ProductService extends IService<Product, Integer> {

    /**
     * Find product by ID with all relationships eagerly loaded
     */
    Product readWithRelations(Integer id);

    /**
     * Find all products with relationships eagerly loaded
     */
    List<Product> getAllWithRelations();

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products by category
     */
    List<Product> findByCategory(Category category);

    /**
     * Find products by category ID
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products by category ID with relationships eagerly loaded
     */
    List<Product> findByCategoryIdWithRelations(Long categoryId);

    /**
     * Find all active products
     */
    List<Product> findActiveProducts();

    /**
     * Find active products by category
     */
    List<Product> findActiveProductsByCategory(Category category);

    /**
     * Search products by name
     */
    List<Product> searchByName(String name);

    /**
     * Find products by price range
     */
    List<Product> findByPriceRange(double minPrice, double maxPrice);

    /**
     * Find active products by price range
     */
    List<Product> findActiveProductsByPriceRange(double minPrice, double maxPrice);

    /**
     * Find latest products
     */
    List<Product> findLatestProducts();

    /**
     * Find products sorted by price ascending
     */
    List<Product> findProductsSortedByPriceAsc();

    /**
     * Find products sorted by price descending
     */
    List<Product> findProductsSortedByPriceDesc();

    /**
     * Activate product
     */
    Product activateProduct(Integer productId);

    /**
     * Deactivate product
     */
    Product deactivateProduct(Integer productId);

    /**
     * Get product image by ID
     */
    ProductImage getImageById(Long imageId);
}
