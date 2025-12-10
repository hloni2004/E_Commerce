package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = CategoryFactory.createCategory("Electronics", "Electronic items");
        testCategory = categoryService.create(testCategory);

        testProduct = ProductFactory.createProduct(
                "Laptop",
                "HP Laptop",
                999.99,
                "SKU-LAPTOP-001",
                testCategory
        );
    }

    @Test
    void testCreate() {
        Product created = productService.create(testProduct);
        assertNotNull(created);
        assertNotNull(created.getProductId());
        assertEquals("Laptop", created.getName());
    }

    @Test
    void testRead() {
        Product created = productService.create(testProduct);
        Product found = productService.read(created.getProductId());
        assertNotNull(found);
        assertEquals(created.getProductId(), found.getProductId());
    }

    @Test
    void testUpdate() {
        Product created = productService.create(testProduct);
        created.setName("Gaming Laptop");
        Product updated = productService.update(created);
        assertNotNull(updated);
        assertEquals("Gaming Laptop", updated.getName());
    }

    @Test
    void testGetAll() {
        productService.create(testProduct);
        Product product2 = ProductFactory.createProduct(
                "Mouse",
                "Wireless Mouse",
                29.99,
                "SKU-MOUSE-001",
                testCategory
        );
        productService.create(product2);

        List<Product> products = productService.getAll();
        assertNotNull(products);
        assertTrue(products.size() >= 2);
    }

    @Test
    void testFindBySku() {
        Product created = productService.create(testProduct);
        Optional<Product> found = productService.findBySku(created.getSku());
        assertTrue(found.isPresent());
        assertEquals(created.getSku(), found.get().getSku());
    }

    @Test
    void testFindByCategory() {
        productService.create(testProduct);
        List<Product> products = productService.findByCategory(testCategory);
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void testFindActiveProducts() {
        testProduct.setActive(true);
        productService.create(testProduct);

        List<Product> activeProducts = productService.findActiveProducts();
        assertNotNull(activeProducts);
        assertTrue(activeProducts.stream().allMatch(Product::isActive));
    }

    @Test
    void testSearchByName() {
        productService.create(testProduct);
        List<Product> products = productService.searchByName("Laptop");
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void testFindByPriceRange() {
        productService.create(testProduct);
        List<Product> products = productService.findByPriceRange(500.0, 1500.0);
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void testActivateProduct() {
        testProduct.setActive(false);
        Product created = productService.create(testProduct);

        Product activated = productService.activateProduct(created.getProductId());
        assertNotNull(activated);
        assertTrue(activated.isActive());
    }

    @Test
    void testDeactivateProduct() {
        testProduct.setActive(true);
        Product created = productService.create(testProduct);

        Product deactivated = productService.deactivateProduct(created.getProductId());
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }
}
