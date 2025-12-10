package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.factory.CategoryFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = CategoryFactory.createCategory("Electronics", "Electronic devices");
    }

    @Test
    void testCreate() {
        Category created = categoryService.create(testCategory);
        assertNotNull(created);
        assertNotNull(created.getCategoryId());
        assertEquals("Electronics", created.getName());
    }

    @Test
    void testRead() {
        Category created = categoryService.create(testCategory);
        Category found = categoryService.read(created.getCategoryId());
        assertNotNull(found);
        assertEquals(created.getCategoryId(), found.getCategoryId());
    }

    @Test
    void testUpdate() {
        Category created = categoryService.create(testCategory);
        created.setDescription("Updated description");
        Category updated = categoryService.update(created);
        assertNotNull(updated);
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void testGetAll() {
        categoryService.create(testCategory);
        Category category2 = CategoryFactory.createCategory("Clothing", "Fashion items");
        categoryService.create(category2);

        List<Category> categories = categoryService.getAll();
        assertNotNull(categories);
        assertTrue(categories.size() >= 2);
    }

    @Test
    void testFindByName() {
        categoryService.create(testCategory);
        Optional<Category> found = categoryService.findByName("Electronics");
        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());
    }

    @Test
    void testFindActiveCategories() {
        testCategory.setActive(true);
        categoryService.create(testCategory);

        List<Category> activeCategories = categoryService.findActiveCategories();
        assertNotNull(activeCategories);
        assertTrue(activeCategories.stream().allMatch(Category::isActive));
    }

    @Test
    void testFindRootCategories() {
        categoryService.create(testCategory);
        List<Category> rootCategories = categoryService.findRootCategories();
        assertNotNull(rootCategories);
        assertFalse(rootCategories.isEmpty());
    }

    @Test
    void testFindSubCategories() {
        Category parent = categoryService.create(testCategory);
        Category subCategory = CategoryFactory.createSubCategory("Laptops", "Laptop computers", parent);
        categoryService.create(subCategory);

        List<Category> subCategories = categoryService.findSubCategories(parent);
        assertNotNull(subCategories);
        assertFalse(subCategories.isEmpty());
    }

    @Test
    void testActivateCategory() {
        testCategory.setActive(false);
        Category created = categoryService.create(testCategory);

        Category activated = categoryService.activateCategory(created.getCategoryId());
        assertNotNull(activated);
        assertTrue(activated.isActive());
    }

    @Test
    void testDeactivateCategory() {
        testCategory.setActive(true);
        Category created = categoryService.create(testCategory);

        Category deactivated = categoryService.deactivateCategory(created.getCategoryId());
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }
}
