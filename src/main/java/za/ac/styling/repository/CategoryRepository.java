package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrue();

    /**
     * Find all inactive categories
     */
    List<Category> findByIsActiveFalse();

    /**
     * Find root categories (categories without parent)
     */
    List<Category> findByParentCategoryIsNull();

    /**
     * Find subcategories of a parent category
     */
    List<Category> findByParentCategory(Category parentCategory);

    /**
     * Find active root categories
     */
    List<Category> findByParentCategoryIsNullAndIsActiveTrue();

    /**
     * Check if category name exists
     */
    boolean existsByName(String name);
}
