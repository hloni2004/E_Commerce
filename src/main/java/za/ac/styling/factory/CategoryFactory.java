package za.ac.styling.factory;

import za.ac.styling.domain.Category;
import za.ac.styling.util.ValidationHelper;

import java.util.ArrayList;

/**
 * Factory class responsible for creating Category objects
 */
public class CategoryFactory {

    /**
     * Creates a new Category with basic information
     */
    public static Category createCategory(String name, String description) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        return Category.builder()
                .name(name)
                .description(description)
                .subCategory(new ArrayList<>())
                .isActive(true)
                .build();
    }

    /**
     * Creates a new Category with image URL
     */
    public static Category createCategory(String name, String description, String imageUrl) {

        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        return Category.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .subCategory(new ArrayList<>())
                .isActive(true)
                .build();
    }

    /**
     * Creates a subcategory under a parent category
     */
    public static Category createSubCategory(String name, String description,
            Category parentCategory) {

        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (parentCategory == null) {
            throw new IllegalArgumentException("Parent category is required for subcategory");
        }

        return Category.builder()
                .name(name)
                .description(description)
                .parentCategory(parentCategory)
                .subCategory(new ArrayList<>())
                .isActive(true)
                .build();
    }

    /**
     * Creates a subcategory with image URL under a parent category
     */
    public static Category createSubCategory(String name, String description, String imageUrl,
            Category parentCategory) {

        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (parentCategory == null) {
            throw new IllegalArgumentException("Parent category is required for subcategory");
        }

        return Category.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .parentCategory(parentCategory)
                .subCategory(new ArrayList<>())
                .isActive(true)
                .build();
    }

    /**
     * Creates an inactive category (for archived categories)
     */
    public static Category createInactiveCategory(String name, String description) {

        Category category = createCategory(name, description);
        category.setActive(false);

        return category;
    }

    /**
     * Creates a root category (main category with no parent)
     */
    public static Category createRootCategory(String name, String description, String imageUrl) {

        return createCategory(name, description, imageUrl);
    }
}
