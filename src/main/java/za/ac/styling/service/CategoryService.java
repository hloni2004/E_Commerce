package za.ac.styling.service;

import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends IService<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findActiveCategories();

    List<Category> findRootCategories();

    List<Category> findSubCategories(Category parentCategory);

    List<Category> findActiveRootCategories();

    Category activateCategory(Long categoryId);

    Category deactivateCategory(Long categoryId);
}
