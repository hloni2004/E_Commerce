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
import za.ac.styling.domain.ProductImage;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;
import za.ac.styling.factory.ProductColourFactory;
import za.ac.styling.factory.ProductColourSizeFactory;
import za.ac.styling.factory.ProductImageFactory;

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
    private ProductColourService productColourService;

    @Autowired
    private ProductColourSizeService productColourSizeService;

    @Autowired
    private ProductImageService productImageService;

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
        colour = productColourService.create(colour);
        assertNotNull(colour);
        assertNotNull(colour.getColourId());
        assertEquals("Black", colour.getName());
        assertEquals(created.getProductId(), colour.getProduct().getProductId());

        // Create size for the colour
        ProductColourSize size = ProductColourSizeFactory.createProductColourSize("M", 100, 10, colour);
        size = productColourSizeService.create(size);
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
        blackColour = productColourService.create(blackColour);

        ProductColour blueColour = ProductColourFactory.createBlueColour(created);
        blueColour = productColourService.create(blueColour);

        // Create sizes for black colour
        ProductColourSize blackSmall = ProductColourSizeFactory.createSmallSize(50, blackColour);
        blackSmall = productColourSizeService.create(blackSmall);

        ProductColourSize blackMedium = ProductColourSizeFactory.createMediumSize(75, blackColour);
        blackMedium = productColourSizeService.create(blackMedium);

        ProductColourSize blackLarge = ProductColourSizeFactory.createLargeSize(100, blackColour);
        blackLarge = productColourSizeService.create(blackLarge);

        // Create sizes for blue colour
        ProductColourSize blueSmall = ProductColourSizeFactory.createSmallSize(40, blueColour);
        blueSmall = productColourSizeService.create(blueSmall);

        ProductColourSize blueMedium = ProductColourSizeFactory.createMediumSize(60, blueColour);
        blueMedium = productColourSizeService.create(blueMedium);

        // Verify all colours are saved
        List<ProductColour> colours = productColourService.findByProduct(created);
        assertNotNull(colours);
        assertEquals(2, colours.size());

        // Verify sizes for black colour
        List<ProductColourSize> blackSizes = productColourSizeService.findByColour(blackColour);
        assertNotNull(blackSizes);
        assertEquals(3, blackSizes.size());

        // Verify sizes for blue colour
        List<ProductColourSize> blueSizes = productColourSizeService.findByColour(blueColour);
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
            colour = productColourService.create(colour);

            for (int i = 0; i < sizes.length; i++) {
                ProductColourSize size = ProductColourSizeFactory.createProductColourSize(
                        sizes[i],
                        stockQuantities[i],
                        10,
                        colour
                );
                productColourSizeService.create(size);
            }
        }

        // Verify the product has all colours
        List<ProductColour> productColours = productColourService.findByProduct(created);
        assertEquals(3, productColours.size());

        // Verify each colour has all sizes
        for (ProductColour colour : productColours) {
            List<ProductColourSize> colourSizes = productColourSizeService.findByColour(colour);
            assertEquals(4, colourSizes.size());
        }

        // Verify total number of size variants
        List<ProductColourSize> allSizes = productColourSizeService.getAll();
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
        colour = productColourService.create(colour);

        // Verify relationship
        Product foundProduct = productService.read(created.getProductId());
        List<ProductColour> colours = productColourService.findByProduct(foundProduct);

        assertNotNull(colours);
        assertEquals(1, colours.size());
        assertEquals("Red", colours.get(0).getName());
        assertEquals(foundProduct.getProductId(), colours.get(0).getProduct().getProductId());
    }


    @Test
    void testCreateCompleteProductWithAllAspects() {
        // ========== STEP 1: Create Category ==========
        Category category = CategoryFactory.createCategory(
                "Clothing_" + System.currentTimeMillis(),
                "Fashion and apparel items"
        );
        category = categoryService.create(category);
        assertNotNull(category.getCategoryId(), "Category should be saved with an ID");
        System.out.println("✓ Category created: " + category.getName());

        // ========== STEP 2: Create Product ==========
        String uniqueId = String.valueOf(System.currentTimeMillis() % 100000); // Get last 5 digits
        Product product = ProductFactory.createProduct(
                "Premium T-Shirt_" + System.currentTimeMillis(),
                "High quality cotton t-shirt with modern design",
                29.99,
                "TSH-" + uniqueId, // SKU format: TSH-12345 (9 characters, within 5-20 limit)
                category
        );
        product = productService.create(product);
        assertNotNull(product.getProductId(), "Product should be saved with an ID");
        System.out.println("✓ Product created: " + product.getName() + " (ID: " + product.getProductId() + ")");

        // ========== STEP 3: Create Multiple Colors ==========
        // Color 1: Black
        ProductColour blackColour = ProductColourFactory.createProductColour("Black", "#000000", product);
        blackColour = productColourService.create(blackColour);
        assertNotNull(blackColour.getColourId(), "Black color should be saved with an ID");
        System.out.println("  ✓ Color 1 created: Black (#000000)");

        // Color 2: White
        ProductColour whiteColour = ProductColourFactory.createProductColour("White", "#FFFFFF", product);
        whiteColour = productColourService.create(whiteColour);
        assertNotNull(whiteColour.getColourId(), "White color should be saved with an ID");
        System.out.println("  ✓ Color 2 created: White (#FFFFFF)");

        // Color 3: Navy Blue
        ProductColour navyColour = ProductColourFactory.createProductColour("Navy Blue", "#000080", product);
        navyColour = productColourService.create(navyColour);
        assertNotNull(navyColour.getColourId(), "Navy color should be saved with an ID");
        System.out.println("  ✓ Color 3 created: Navy Blue (#000080)");

        // ========== STEP 4: Create Multiple Sizes for Each Color ==========
        // Sizes for Black Color
        ProductColourSize blackXS = ProductColourSizeFactory.createProductColourSize("XS", 50, 10, blackColour);
        blackXS = productColourSizeService.create(blackXS);
        System.out.println("    ✓ Black - Size XS: 50 units (Reorder level: 10)");

        ProductColourSize blackS = ProductColourSizeFactory.createProductColourSize("S", 75, 10, blackColour);
        blackS = productColourSizeService.create(blackS);
        System.out.println("    ✓ Black - Size S: 75 units");

        ProductColourSize blackM = ProductColourSizeFactory.createProductColourSize("M", 100, 15, blackColour);
        blackM = productColourSizeService.create(blackM);
        System.out.println("    ✓ Black - Size M: 100 units");

        ProductColourSize blackL = ProductColourSizeFactory.createProductColourSize("L", 80, 12, blackColour);
        blackL = productColourSizeService.create(blackL);
        System.out.println("    ✓ Black - Size L: 80 units");

        ProductColourSize blackXL = ProductColourSizeFactory.createProductColourSize("XL", 60, 10, blackColour);
        blackXL = productColourSizeService.create(blackXL);
        System.out.println("    ✓ Black - Size XL: 60 units");

        // Sizes for White Color
        ProductColourSize whiteS = ProductColourSizeFactory.createProductColourSize("S", 65, 10, whiteColour);
        whiteS = productColourSizeService.create(whiteS);
        System.out.println("    ✓ White - Size S: 65 units");

        ProductColourSize whiteM = ProductColourSizeFactory.createProductColourSize("M", 90, 15, whiteColour);
        whiteM = productColourSizeService.create(whiteM);
        System.out.println("    ✓ White - Size M: 90 units");

        ProductColourSize whiteL = ProductColourSizeFactory.createProductColourSize("L", 70, 12, whiteColour);
        whiteL = productColourSizeService.create(whiteL);
        System.out.println("    ✓ White - Size L: 70 units");

        // Sizes for Navy Color
        ProductColourSize navyM = ProductColourSizeFactory.createProductColourSize("M", 85, 15, navyColour);
        navyM = productColourSizeService.create(navyM);
        System.out.println("    ✓ Navy - Size M: 85 units");

        ProductColourSize navyL = ProductColourSizeFactory.createProductColourSize("L", 75, 12, navyColour);
        navyL = productColourSizeService.create(navyL);
        System.out.println("    ✓ Navy - Size L: 75 units");

        ProductColourSize navyXL = ProductColourSizeFactory.createProductColourSize("XL", 55, 10, navyColour);
        navyXL = productColourSizeService.create(navyXL);
        System.out.println("    ✓ Navy - Size XL: 55 units");

        // ========== STEP 5: Create Multiple Product Images ==========
        // IMPORTANT: Re-fetch the product to ensure we have the latest state
        Product productForImages = productService.read(product.getProductId());

        // Primary Image
        ProductImage primaryImage = ProductImageFactory.createPrimaryProductImage(
                productForImages,
                "https://example.com/images/tshirt-primary.jpg",
                productForImages.getName() + " - Main View"
        );
        primaryImage = productImageService.create(primaryImage);
        assertNotNull(primaryImage.getImageId(), "Primary image should be saved");
        assertTrue(primaryImage.isPrimary(), "Image should be marked as primary");
        assertEquals(0, primaryImage.getDisplayOrder(), "Primary image should have display order 0");
        System.out.println("  ✓ Image 1 (Primary): Main View - Display Order: 0");

        // Secondary Images
        ProductImage frontImage = ProductImageFactory.createSecondaryProductImage(
                productForImages,
                "https://example.com/images/tshirt-front.jpg",
                productForImages.getName() + " - Front View",
                1
        );
        frontImage = productImageService.create(frontImage);
        assertFalse(frontImage.isPrimary(), "Secondary image should not be primary");
        System.out.println("  ✓ Image 2: Front View - Display Order: 1");

        ProductImage backImage = ProductImageFactory.createSecondaryProductImage(
                productForImages,
                "https://example.com/images/tshirt-back.jpg",
                productForImages.getName() + " - Back View",
                2
        );
        backImage = productImageService.create(backImage);
        System.out.println("  ✓ Image 3: Back View - Display Order: 2");

        ProductImage sideImage = ProductImageFactory.createSecondaryProductImage(
                productForImages,
                "https://example.com/images/tshirt-side.jpg",
                productForImages.getName() + " - Side View",
                3
        );
        sideImage = productImageService.create(sideImage);
        System.out.println("  ✓ Image 4: Side View - Display Order: 3");

        ProductImage detailImage = ProductImageFactory.createSecondaryProductImage(
                productForImages,
                "https://example.com/images/tshirt-detail.jpg",
                productForImages.getName() + " - Material Detail",
                4
        );
        detailImage = productImageService.create(detailImage);
        System.out.println("  ✓ Image 5: Detail View - Display Order: 4");

        // ========== STEP 6: Verify All Relationships ==========
        Product retrievedProduct = productService.read(product.getProductId());
        assertNotNull(retrievedProduct, "Product should be retrievable from database");

        // Verify colors count
        List<ProductColour> colours = productColourService.findByProduct(retrievedProduct);
        assertEquals(3, colours.size(), "Product should have 3 colors");

        // Verify total sizes count
        int totalSizes = 0;
        for (ProductColour colour : colours) {
            List<ProductColourSize> sizes = productColourSizeService.findByColour(colour);
            totalSizes += sizes.size();
        }
        assertEquals(11, totalSizes, "Product should have 11 total size variants");

        // Verify images count
        List<ProductImage> images = productImageService.findByProduct(retrievedProduct);
        System.out.println("  DEBUG: Found " + images.size() + " images in database");

        // Print all images for debugging
        for (ProductImage img : images) {
            System.out.println("    - Image ID: " + img.getImageId() +
                    ", URL: " + img.getImageUrl() +
                    ", Primary: " + img.isPrimary() +
                    ", Display Order: " + img.getDisplayOrder());
        }

        assertEquals(5, images.size(), "Product should have 5 images");

        // Verify primary image exists
        long primaryImageCount = images.stream().filter(ProductImage::isPrimary).count();
        assertEquals(1, primaryImageCount, "Product should have exactly 1 primary image");

        // ========== STEP 7: Calculate Total Stock ==========
        int totalStock = 0;
        for (ProductColour colour : colours) {
            List<ProductColourSize> sizes = productColourSizeService.findByColour(colour);
            for (ProductColourSize size : sizes) {
                totalStock += size.getStockQuantity();
            }
        }

        System.out.println("\n========== COMPLETE PRODUCT SUMMARY ==========");
        System.out.println("Product: " + product.getName());
        System.out.println("Category: " + category.getName());
        System.out.println("Colors: 3 (Black, White, Navy Blue)");
        System.out.println("Size Variants: 11");
        System.out.println("Total Stock: " + totalStock + " units");
        System.out.println("Images: " + images.size() + " (1 primary, " + (images.size() - 1) + " secondary)");
        System.out.println("Base Price: R" + product.getBasePrice());
        System.out.println("SKU: " + product.getSku());
        System.out.println("==============================================\n");

        // Final assertions
        assertTrue(totalStock > 0, "Product should have stock available");
        assertEquals(805, totalStock, "Total stock should be 805 units");

        System.out.println(" COMPREHENSIVE TEST PASSED: Complete product created with all aspects!");
    }
}