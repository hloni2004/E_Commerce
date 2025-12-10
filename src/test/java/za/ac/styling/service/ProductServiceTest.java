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
import za.ac.styling.repository.ProductColourRepository;
import za.ac.styling.repository.ProductColourSizeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductColourRepository productColourRepository;

    @Autowired
    private ProductColourSizeRepository productColourSizeRepository;

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
    void testCreateWithColourAndSize() {
        // Create product
        Product created = productService.create(testProduct);
        assertNotNull(created);
        assertNotNull(created.getProductId());

        // Create colour for the product
        ProductColour colour = ProductColourFactory.createProductColour("Black", "#000000", created);
        colour = productColourRepository.save(colour);
        assertNotNull(colour);
        assertNotNull(colour.getColourId());
        assertEquals("Black", colour.getName());
        assertEquals(created.getProductId(), colour.getProduct().getProductId());

        // Create size for the colour
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 100, 10, colour);
        size = productColourSizeRepository.save(size);
        assertNotNull(size);
        assertNotNull(size.getSizeId());
        assertEquals("M", size.getSizeName());
        assertEquals(100, size.getStockQuantity());
        assertEquals(colour.getColourId(), size.getColour().getColourId());
    }

    @Test
    void testCreateProductWithMultipleColoursAndSizes() {
        // Create product
        Product created = productService.create(testProduct);
        assertNotNull(created);

        // Create multiple colours
        ProductColour blackColour = ProductColourFactory.createBlackColour(created);
        blackColour = productColourRepository.save(blackColour);

        ProductColour blueColour = ProductColourFactory.createBlueColour(created);
        blueColour = productColourRepository.save(blueColour);

        // Create sizes for black colour
        ProductColourSize blackSmall = ProductColourSizeFactory.createSmallSize(50, blackColour);
        blackSmall = productColourSizeRepository.save(blackSmall);

        ProductColourSize blackMedium = ProductColourSizeFactory.createMediumSize(75, blackColour);
        blackMedium = productColourSizeRepository.save(blackMedium);

        ProductColourSize blackLarge = ProductColourSizeFactory.createLargeSize(100, blackColour);
        blackLarge = productColourSizeRepository.save(blackLarge);

        // Create sizes for blue colour
        ProductColourSize blueSmall = ProductColourSizeFactory.createSmallSize(40, blueColour);
        blueSmall = productColourSizeRepository.save(blueSmall);

        ProductColourSize blueMedium = ProductColourSizeFactory.createMediumSize(60, blueColour);
        blueMedium = productColourSizeRepository.save(blueMedium);

        // Verify all colours are saved
        List<ProductColour> colours = productColourRepository.findByProduct(created);
        assertNotNull(colours);
        assertEquals(2, colours.size());

        // Verify sizes for black colour
        List<ProductColourSize> blackSizes = productColourSizeRepository.findByColour(blackColour);
        assertNotNull(blackSizes);
        assertEquals(3, blackSizes.size());

        // Verify sizes for blue colour
        List<ProductColourSize> blueSizes = productColourSizeRepository.findByColour(blueColour);
        assertNotNull(blueSizes);
        assertEquals(2, blueSizes.size());
    }

    @Test
    void testCreateCompleteProductWithVariants() {
        // Create product
        Product created = productService.create(testProduct);

        // Define colours and their sizes with stock
        String[][] colourData = {
                {"Black", "#000000"},
                {"White", "#FFFFFF"},
                {"Blue", "#0000FF"}
        };

        String[] sizes = {"S", "M", "L", "XL"};
        int[] stockQuantities = {50, 75, 100, 60};

        // Create all colour-size combinations
        for (String[] colourInfo : colourData) {
            ProductColour colour = ProductColourFactory.createProductColour(
                    colourInfo[0],
                    colourInfo[1],
                    created
            );
            colour = productColourRepository.save(colour);

            for (int i = 0; i < sizes.length; i++) {
                ProductColourSize size = ProductColourSizeFactory.createProductColourSize(
                        sizes[i],
                        stockQuantities[i],
                        10,
                        colour
                );
                productColourSizeRepository.save(size);
            }
        }

        // Verify the product has all colours
        List<ProductColour> productColours = productColourRepository.findByProduct(created);
        assertEquals(3, productColours.size());

        // Verify each colour has all sizes
        for (ProductColour colour : productColours) {
            List<ProductColourSize> colourSizes = productColourSizeRepository.findByColour(colour);
            assertEquals(4, colourSizes.size());
        }

        // Verify total number of size variants
        List<ProductColourSize> allSizes = productColourSizeRepository.findAll();
        assertTrue(allSizes.size() >= 12); // At least 3 colours × 4 sizes = 12 variants
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

    @Test
    void testProductColourRelationship() {
        Product created = productService.create(testProduct);

        // Create colour
        ProductColour colour = ProductColourFactory.createRedColour(created);
        colour = productColourRepository.save(colour);

        // Verify relationship
        Product foundProduct = productService.read(created.getProductId());
        List<ProductColour> colours = productColourRepository.findByProduct(foundProduct);

        assertNotNull(colours);
        assertEquals(1, colours.size());
        assertEquals("Red", colours.get(0).getName());
        assertEquals(foundProduct.getProductId(), colours.get(0).getProduct().getProductId());
    }

    @Test
    void testCompleteProductWithStockManagement() {
        // Create product
        Product created = productService.create(testProduct);

        // Create colour
        ProductColour colour = ProductColourFactory.createBlackColour(created);
        colour = productColourRepository.save(colour);

        // Create size with stock
        ProductColourSize size = ProductColourSizeFactory.createMediumSize(100, colour);
        size = productColourSizeRepository.save(size);

        // Reserve stock
        ProductColourSize updatedSize = ProductColourSizeFactory.reserveStock(size, 10);
        updatedSize = productColourSizeRepository.save(updatedSize);

        // Verify stock reservation
        assertEquals(10, updatedSize.getReservedQuantity());
        assertEquals(100, updatedSize.getStockQuantity());

        // Complete sale
        updatedSize = ProductColourSizeFactory.completeSale(updatedSize, 10);
        updatedSize = productColourSizeRepository.save(updatedSize);

        // Verify stock after sale
        assertEquals(90, updatedSize.getStockQuantity());
        assertEquals(0, updatedSize.getReservedQuantity());
    }
}