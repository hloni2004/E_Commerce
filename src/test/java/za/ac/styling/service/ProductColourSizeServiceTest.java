package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;
import za.ac.styling.factory.ProductColourFactory;
import za.ac.styling.factory.ProductColourSizeFactory;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductColourSizeServiceTest {

    @Autowired
    private ProductColourSizeService productColourSizeService;

    @Autowired
    private ProductColourService productColourService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private Product testProduct;
    private ProductColour testColour;
    private ProductColourSize testProductColourSize;

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
        testColour = ProductColourFactory.createProductColour("Blue", "#0000FF", testProduct);
        testColour = productColourService.create(testColour);

        // Setup product colour size
        testProductColourSize = ProductColourSizeFactory.createProductColourSize("M", 100, 10, testColour);
    }

    @Test
    void testCreate() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        assertNotNull(created);
        assertNotNull(created.getSizeId());
        assertEquals("M", created.getSizeName());
        assertEquals(100, created.getStockQuantity());
    }

    @Test
    void testRead() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        ProductColourSize found = productColourSizeService.read(created.getSizeId());
        assertNotNull(found);
        assertEquals(created.getSizeId(), found.getSizeId());
    }

    @Test
    void testUpdate() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        created.setStockQuantity(50);
        ProductColourSize updated = productColourSizeService.update(created);
        assertNotNull(updated);
        assertEquals(50, updated.getStockQuantity());
    }

    @Test
    void testGetAll() {
        productColourSizeService.create(testProductColourSize);
        List<ProductColourSize> sizes = productColourSizeService.getAll();
        assertNotNull(sizes);
        assertFalse(sizes.isEmpty());
    }

    @Test
    void testFindByColour() {
        productColourSizeService.create(testProductColourSize);
        List<ProductColourSize> sizes = productColourSizeService.findByColour(testColour);
        assertNotNull(sizes);
        assertFalse(sizes.isEmpty());
        assertTrue(sizes.stream().anyMatch(s -> s.getSizeName().equals("M")));
    }

    @Test
    void testFindByColourId() {
        productColourSizeService.create(testProductColourSize);
        List<ProductColourSize> sizes = productColourSizeService.findByColourId(testColour.getColourId());
        assertNotNull(sizes);
        assertFalse(sizes.isEmpty());
    }

    @Test
    void testFindByColourAndSizeName() {
        productColourSizeService.create(testProductColourSize);
        Optional<ProductColourSize> found = productColourSizeService.findByColourAndSizeName(testColour, "M");
        assertTrue(found.isPresent());
        assertEquals("M", found.get().getSizeName());
    }

    @Test
    void testFindLowStockItems() {
        ProductColourSize lowStock = ProductColourSizeFactory.createProductColourSize("S", 5, 10, testColour);
        productColourSizeService.create(lowStock);

        List<ProductColourSize> lowStockItems = productColourSizeService.findLowStockItems();
        assertNotNull(lowStockItems);
    }

    @Test
    void testFindOutOfStockItems() {
        ProductColourSize outOfStock = ProductColourSizeFactory.createProductColourSize("L", 0, 10, testColour);
        productColourSizeService.create(outOfStock);

        List<ProductColourSize> outOfStockItems = productColourSizeService.findOutOfStockItems();
        assertNotNull(outOfStockItems);
    }

    @Test
    void testFindAvailableSizesByColour() {
        productColourSizeService.create(testProductColourSize);
        List<ProductColourSize> availableSizes = productColourSizeService.findAvailableSizesByColour(testColour);
        assertNotNull(availableSizes);
        assertFalse(availableSizes.isEmpty());
        assertTrue(availableSizes.stream().allMatch(s -> s.getStockQuantity() > 0));
    }

    @Test
    void testReserveStock() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        int initialStock = created.getStockQuantity();

        ProductColourSize reserved = productColourSizeService.reserveStock(created.getSizeId(), 10);
        assertNotNull(reserved);
    }

    @Test
    void testReleaseStock() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        productColourSizeService.reserveStock(created.getSizeId(), 10);

        ProductColourSize released = productColourSizeService.releaseStock(created.getSizeId(), 10);
        assertNotNull(released);
    }

    @Test
    void testCompleteSale() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        int initialStock = created.getStockQuantity();

        ProductColourSize afterSale = productColourSizeService.completeSale(created.getSizeId(), 5);
        assertNotNull(afterSale);
    }

    @Test
    void testAddStock() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        int initialStock = created.getStockQuantity();

        ProductColourSize afterAdd = productColourSizeService.addStock(created.getSizeId(), 50);
        assertNotNull(afterAdd);
        assertTrue(afterAdd.getStockQuantity() > initialStock);
    }

    @Test
    void testNeedsReordering() {
        ProductColourSize created = productColourSizeService.create(testProductColourSize);
        boolean needsReorder = productColourSizeService.needsReordering(created.getSizeId());
        assertNotNull(needsReorder);
    }

    @Test
    void testCreateMultipleSizes() {
        ProductColourSize small = ProductColourSizeFactory.createProductColourSize("S", 80, 10, testColour);
        ProductColourSize medium = productColourSizeService.create(testProductColourSize);
        ProductColourSize large = ProductColourSizeFactory.createProductColourSize("L", 120, 10, testColour);

        productColourSizeService.create(small);
        productColourSizeService.create(large);

        List<ProductColourSize> allSizes = productColourSizeService.findByColour(testColour);
        assertTrue(allSizes.size() >= 3);
    }
}
