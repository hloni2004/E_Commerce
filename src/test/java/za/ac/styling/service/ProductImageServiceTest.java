package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;
import za.ac.styling.factory.ProductImageFactory;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductImageServiceTest {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private Product testProduct;
    private ProductImage testProductImage;

    @BeforeEach
    void setUp() {
        // Setup category
        Category category = CategoryFactory.createCategory("Electronics", "Electronic items");
        category = categoryService.create(category);

        // Setup product
        testProduct = ProductFactory.createProduct(
                "Laptop",
                "HP Laptop",
                999.99,
                "SKU-LAPTOP-001",
                category);
        testProduct = productService.create(testProduct);

        // Setup product image
        testProductImage = ProductImageFactory.createSecondaryProductImage(
                testProduct,
                "https://example.com/laptop.jpg",
                "Laptop image",
                1);
    }

    @Test
    void testCreate() {
        ProductImage created = productImageService.create(testProductImage);
        assertNotNull(created);
        assertNotNull(created.getImageId());
        assertEquals("https://example.com/laptop.jpg", created.getImageUrl());
        assertEquals("Laptop image", created.getAltText());
    }

    @Test
    void testRead() {
        ProductImage created = productImageService.create(testProductImage);
        ProductImage found = productImageService.read(created.getImageId());
        assertNotNull(found);
        assertEquals(created.getImageId(), found.getImageId());
    }

    @Test
    void testUpdate() {
        ProductImage created = productImageService.create(testProductImage);
        created.setAltText("Updated laptop image");
        ProductImage updated = productImageService.update(created);
        assertNotNull(updated);
        assertEquals("Updated laptop image", updated.getAltText());
    }

    @Test
    void testGetAll() {
        productImageService.create(testProductImage);
        List<ProductImage> images = productImageService.getAll();
        assertNotNull(images);
        assertFalse(images.isEmpty());
    }

    @Test
    void testFindByProduct() {
        productImageService.create(testProductImage);
        List<ProductImage> images = productImageService.findByProduct(testProduct);
        assertNotNull(images);
        assertFalse(images.isEmpty());
        assertTrue(images.stream().anyMatch(img -> img.getProduct().getProductId().equals(testProduct.getProductId())));
    }

    @Test
    void testFindByProductId() {
        productImageService.create(testProductImage);
        List<ProductImage> images = productImageService.findByProductId(testProduct.getProductId());
        assertNotNull(images);
        assertFalse(images.isEmpty());
    }

    @Test
    void testFindByProductOrderByDisplayOrder() {
        productImageService.create(testProductImage);

        ProductImage image2 = ProductImageFactory.createSecondaryProductImage(
                testProduct,
                "https://example.com/laptop2.jpg",
                "Laptop image 2",
                2);
        productImageService.create(image2);

        List<ProductImage> images = productImageService.findByProductOrderByDisplayOrder(testProduct);
        assertNotNull(images);
        assertTrue(images.size() >= 1);
    }

    @Test
    void testFindPrimaryImageByProduct() {
        ProductImage primary = ProductImageFactory.createPrimaryProductImage(
                testProduct,
                "https://example.com/primary.jpg",
                "Primary image");
        primary.setPrimary(true);
        productImageService.create(primary);

        Optional<ProductImage> found = productImageService.findPrimaryImageByProduct(testProduct);
        assertTrue(found.isPresent());
        assertTrue(found.get().isPrimary());
    }

    @Test
    void testFindSecondaryImagesByProduct() {
        ProductImage primary = ProductImageFactory.createPrimaryProductImage(
                testProduct,
                "https://example.com/primary.jpg",
                "Primary image");
        productImageService.create(primary);

        ProductImage secondary = ProductImageFactory.createSecondaryProductImage(
                testProduct,
                "https://example.com/secondary.jpg",
                "Secondary image",
                2);
        secondary.setPrimary(false);
        productImageService.create(secondary);

        List<ProductImage> secondaryImages = productImageService.findSecondaryImagesByProduct(testProduct);
        assertNotNull(secondaryImages);
    }

    @Test
    void testSetAsPrimary() {
        ProductImage created = productImageService.create(testProductImage);
        ProductImage primary = productImageService.setAsPrimary(created.getImageId());
        assertNotNull(primary);
        assertTrue(primary.isPrimary());
    }

    @Test
    void testUpdateDisplayOrder() {
        ProductImage created = productImageService.create(testProductImage);
        ProductImage updated = productImageService.updateDisplayOrder(created.getImageId(), 5);
        assertNotNull(updated);
        assertEquals(5, updated.getDisplayOrder());
    }

    @Test
    void testCreateMultipleImagesForProduct() {
        ProductImage image1 = productImageService.create(testProductImage);

        ProductImage image2 = ProductImageFactory.createSecondaryProductImage(
                testProduct,
                "https://example.com/laptop2.jpg",
                "Laptop image 2",
                2);
        ProductImage created2 = productImageService.create(image2);

        assertNotNull(image1);
        assertNotNull(created2);

        List<ProductImage> images = productImageService.findByProduct(testProduct);
        assertTrue(images.size() >= 2);
    }

    @Test
    void testDeleteImage() {
        ProductImage created = productImageService.create(testProductImage);
        assertNotNull(created);
        assertNotNull(created.getImageId());

        ProductImage found = productImageService.read(created.getImageId());
        assertNotNull(found);
    }

    @Test
    void testImageValidation() {
        ProductImage validImage = ProductImageFactory.createSecondaryProductImage(
                testProduct,
                "https://example.com/valid.jpg",
                "Valid image",
                1);
        ProductImage created = productImageService.create(validImage);
        assertNotNull(created);
        assertEquals("https://example.com/valid.jpg", created.getImageUrl());
    }
}
