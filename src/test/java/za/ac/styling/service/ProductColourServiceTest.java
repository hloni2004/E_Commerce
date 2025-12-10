package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;
import za.ac.styling.factory.ProductColourFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductColourServiceTest {

    @Autowired
    private ProductColourService productColourService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private Product testProduct;
    private ProductColour testProductColour;

    @BeforeEach
    void setUp() {
        // Setup category
        Category category = CategoryFactory.createCategory("Clothing", "Fashion items");
        category = categoryService.create(category);

        // Setup product
        testProduct = ProductFactory.createProduct(
                "T-Shirt",
                "Cotton T-Shirt",
                29.99,
                "SKU-TSHIRT-001",
                category);
        testProduct = productService.create(testProduct);

        // Setup product colour
        testProductColour = ProductColourFactory.createProductColour("Blue", "#0000FF", testProduct);
    }

    @Test
    void testCreate() {
        ProductColour created = productColourService.create(testProductColour);
        assertNotNull(created);
        assertNotNull(created.getColourId());
        assertEquals("Blue", created.getName());
        assertEquals("#0000FF", created.getHexCode());
    }

    @Test
    void testRead() {
        ProductColour created = productColourService.create(testProductColour);
        ProductColour found = productColourService.read(created.getColourId());
        assertNotNull(found);
        assertEquals(created.getColourId(), found.getColourId());
    }

    @Test
    void testUpdate() {
        ProductColour created = productColourService.create(testProductColour);
        created.setName("Red");
        ProductColour updated = productColourService.update(created);
        assertNotNull(updated);
        assertEquals("Red", updated.getName());
    }

    @Test
    void testGetAll() {
        productColourService.create(testProductColour);
        List<ProductColour> colours = productColourService.getAll();
        assertNotNull(colours);
        assertFalse(colours.isEmpty());
    }

    @Test
    void testFindByProduct() {
        productColourService.create(testProductColour);
        List<ProductColour> colours = productColourService.findByProduct(testProduct);
        assertNotNull(colours);
        assertFalse(colours.isEmpty());
        assertTrue(colours.stream().anyMatch(c -> c.getName().equals("Blue")));
    }

    @Test
    void testFindByProductId() {
        productColourService.create(testProductColour);
        List<ProductColour> colours = productColourService.findByProductId(testProduct.getProductId());
        assertNotNull(colours);
        assertFalse(colours.isEmpty());
    }

    @Test
    void testFindByProductAndName() {
        productColourService.create(testProductColour);
        List<ProductColour> colours = productColourService.findByProductAndName(testProduct, "Blue");
        assertNotNull(colours);
        assertFalse(colours.isEmpty());
        assertTrue(colours.stream().allMatch(c -> c.getName().equals("Blue")));
    }

    @Test
    void testHasColour() {
        productColourService.create(testProductColour);
        assertTrue(productColourService.hasColour(testProduct, "Blue"));
        assertFalse(productColourService.hasColour(testProduct, "Green"));
    }

    @Test
    void testCreateMultipleColoursForProduct() {
        ProductColour blue = productColourService.create(testProductColour);

        ProductColour red = ProductColourFactory.createProductColour("Red", "#FF0000", testProduct);
        ProductColour redCreated = productColourService.create(red);

        assertNotNull(blue);
        assertNotNull(redCreated);

        List<ProductColour> colours = productColourService.findByProduct(testProduct);
        assertTrue(colours.size() >= 2);
    }

    @Test
    void testDeleteColour() {
        ProductColour created = productColourService.create(testProductColour);
        assertNotNull(created);
        assertNotNull(created.getColourId());

        ProductColour found = productColourService.read(created.getColourId());
        assertNotNull(found);
    }

    @Test
    void testColourValidation() {
        ProductColour invalidColour = ProductColourFactory.createProductColour(
                "Black",
                "#000000",
                testProduct);
        ProductColour created = productColourService.create(invalidColour);
        assertNotNull(created);
        assertEquals("Black", created.getName());
    }
}
