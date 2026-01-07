package za.ac.styling.factory;

import za.ac.styling.domain.Category;
import za.ac.styling.util.ValidationHelper;

import java.util.ArrayList;

public class CategoryFactory {

    public static Category createCategory(String name, String description) {

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

    public static Category createInactiveCategory(String name, String description) {

        Category category = createCategory(name, description);
        category.setActive(false);

        return category;
    }

    public static Category createRootCategory(String name, String description, String imageUrl) {

        return createCategory(name, description, imageUrl);
    }
}
