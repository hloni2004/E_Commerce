package za.ac.styling.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"category", "colours", "colours.sizes", "images", "primaryImage"})
    @Query("SELECT p FROM Product p WHERE p.productId = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithRelations(Integer id);

    @EntityGraph(attributePaths = {"category", "colours", "colours.sizes", "images", "primaryImage"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAllWithRelations();

    Optional<Product> findByProductIdAndDeletedAtIsNull(Integer productId);

    @EntityGraph(attributePaths = {"category", "colours", "colours.sizes", "images", "primaryImage"})
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdWithRelationsIncludingDeleted(Integer id);

    @EntityGraph(attributePaths = {"category", "colours", "colours.sizes", "images", "primaryImage"})
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryCategoryIdWithRelations(Long categoryId);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(Category category);

    List<Product> findByCategoryCategoryId(Long categoryId);

    List<Product> findByIsActiveTrueAndDeletedAtIsNull();

    List<Product> findByIsActiveFalseAndDeletedAtIsNull();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL")
    List<Product> findByIsActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.isActive = false AND p.deletedAt IS NULL")
    List<Product> findByIsActiveFalse();

    List<Product> findByCategoryAndIsActiveTrueAndDeletedAtIsNull(Category category);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.isActive = true AND p.deletedAt IS NULL")
    List<Product> findByCategoryAndIsActiveTrue(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :minPrice AND :maxPrice AND p.deletedAt IS NULL")
    List<Product> findByBasePriceBetween(double minPrice, double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :minPrice AND :maxPrice AND p.isActive = true AND p.deletedAt IS NULL")
    List<Product> findByBasePriceBetweenAndIsActiveTrue(double minPrice, double maxPrice);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.basePrice ASC")
    List<Product> findByIsActiveTrueOrderByBasePriceAsc();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.basePrice DESC")
    List<Product> findByIsActiveTrueOrderByBasePriceDesc();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Product> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p")
    List<Product> findAllIncludingDeleted();

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAllNotDeleted();
}
