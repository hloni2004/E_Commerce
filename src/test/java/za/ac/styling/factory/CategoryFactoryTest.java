package za.ac.styling.factory;

import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;

import static org.junit.jupiter.api.Assertions.*;

class CategoryFactoryTest {

    @Test
    void createCategory_WithValidData_ShouldCreateCategory() {
        // Arrange
        String name = "Electronics";
        String description = "Electronic devices and accessories";

        // Act
        Category category = CategoryFactory.createCategory(name, description);

        // Assert
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        assertNotNull(category.getSubCategory());
        assertTrue(category.getSubCategory().isEmpty());
        assertTrue(category.isActive());
        assertNull(category.getImageUrl());
    }

    @Test
    void createCategory_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CategoryFactory.createCategory("", "Description")
        );

        assertEquals("Category name cannot be empty", exception.getMessage());
    }

    @Test
    void createCategory_WithNullName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CategoryFactory.createCategory(null, "Description")
        );

        assertEquals("Category name cannot be empty", exception.getMessage());
    }

    @Test
    void createCategory_WithImageUrl_ShouldCreateCategoryWithImage() {
        // Arrange
        String name = "Fashion";
        String description = "Clothing and accessories";
        String imageUrl = "https://example.com/fashion.jpg";

        // Act
        Category category = CategoryFactory.createCategory(name, description, imageUrl);

        // Assert
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        assertEquals(imageUrl, category.getImageUrl());
        assertTrue(category.isActive());
    }

    @Test
    void createSubCategory_WithValidData_ShouldCreateSubCategory() {
        // Arrange
        Category parent = CategoryFactory.createCategory("Electronics", "Parent category");
        String name = "Smartphones";
        String description = "Mobile phones";

        // Act
        Category subCategory = CategoryFactory.createSubCategory(name, description, parent);

        // Assert
        assertNotNull(subCategory);
        assertEquals(name, subCategory.getName());
        assertEquals(description, subCategory.getDescription());
        assertEquals(parent, subCategory.getParentCategory());
        assertTrue(subCategory.isActive());
    }

    @Test
    void createSubCategory_WithNullParent_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CategoryFactory.createSubCategory("SubCategory", "Description", null)
        );

        assertEquals("Parent category is required for subcategory", exception.getMessage());
    }

    @Test
    void createSubCategory_WithEmptyName_ShouldThrowException() {
        // Arrange
        Category parent = CategoryFactory.createCategory("Parent", "Description");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CategoryFactory.createSubCategory("", "Description", parent)
        );

        assertEquals("Category name cannot be empty", exception.getMessage());
    }

    @Test
    void createSubCategory_WithImageUrl_ShouldCreateSubCategoryWithImage() {
        // Arrange
        Category parent = CategoryFactory.createCategory("Electronics", "Parent category");
        String name = "Laptops";
        String description = "Portable computers";
        String imageUrl = "https://example.com/laptops.jpg";

        // Act
        Category subCategory = CategoryFactory.createSubCategory(name, description, imageUrl, parent);

        // Assert
        assertNotNull(subCategory);
        assertEquals(name, subCategory.getName());
        assertEquals(imageUrl, subCategory.getImageUrl());
        assertEquals(parent, subCategory.getParentCategory());
    }

    @Test
    void createInactiveCategory_ShouldCreateInactiveCategory() {
        // Arrange
        String name = "Archived";
        String description = "Archived category";

        // Act
        Category category = CategoryFactory.createInactiveCategory(name, description);

        // Assert
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertFalse(category.isActive());
    }

    @Test
    void createRootCategory_ShouldCreateRootCategory() {
        // Arrange
        String name = "Home & Garden";
        String description = "Home and garden products";
        String imageUrl = "https://example.com/home.jpg";

        // Act
        Category category = CategoryFactory.createRootCategory(name, description, imageUrl);

        // Assert
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        assertEquals(imageUrl, category.getImageUrl());
        assertNull(category.getParentCategory());
        assertTrue(category.isActive());
    }
}
