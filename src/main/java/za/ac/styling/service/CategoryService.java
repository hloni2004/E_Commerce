package za.ac.styling.service;

import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Category entity
 */
public interface CategoryService extends IService<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Find all active categories
     */
    List<Category> findActiveCategories();

    /**
     * Find all root categories
     */
    List<Category> findRootCategories();

    /**
     * Find subcategories of a parent category
     */
    List<Category> findSubCategories(Category parentCategory);

    /**
     * Find active root categories
     */
    List<Category> findActiveRootCategories();

    /**
     * Activate category
     */
    Category activateCategory(Long categoryId);

    /**
     * Deactivate category
     */
    Category deactivateCategory(Long categoryId);
}
