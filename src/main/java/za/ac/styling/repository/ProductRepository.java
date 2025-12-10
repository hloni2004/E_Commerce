package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

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
    List<Product> findByCategoryCategoryId(Long categoryId);

    /**
     * Find all active products
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find all inactive products
     */
    List<Product> findByIsActiveFalse();

    /**
     * Find active products by category
     */
    List<Product> findByCategoryAndIsActiveTrue(Category category);

    /**
     * Search products by name
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by price range
     */
    List<Product> findByBasePriceBetween(double minPrice, double maxPrice);

    /**
     * Find active products by price range
     */
    List<Product> findByBasePriceBetweenAndIsActiveTrue(double minPrice, double maxPrice);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find products ordered by price ascending
     */
    List<Product> findByIsActiveTrueOrderByBasePriceAsc();

    /**
     * Find products ordered by price descending
     */
    List<Product> findByIsActiveTrueOrderByBasePriceDesc();

    /**
     * Find latest products
     */
    List<Product> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
}
