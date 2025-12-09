package za.ac.styling.factory;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.util.ValidationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Factory class responsible for creating Product objects
 */
public class ProductFactory {

    /**
     * Creates a new Product with basic information
     */
    public static Product createProduct(String name, String description, double basePrice,
            String sku, Category category) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (!ValidationHelper.isValidPrice(basePrice)) {
            throw new IllegalArgumentException("Invalid base price");
        }

        if (!ValidationHelper.isValidSKU(sku)) {
            throw new IllegalArgumentException("Invalid SKU format");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }

        return Product.builder()
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .comparePrice(0.0)
                .sku(sku.toUpperCase())
                .category(category)
                .colours(new ArrayList<>())
                .images(new ArrayList<>())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    /**
     * Creates a new Product with complete information including compare price and
     * weight
     */
    public static Product createProduct(String name, String description, double basePrice,
            double comparePrice, String sku, double weight,
            Category category) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (!ValidationHelper.isValidPrice(basePrice)) {
            throw new IllegalArgumentException("Invalid base price");
        }

        if (!ValidationHelper.isValidPrice(comparePrice)) {
            throw new IllegalArgumentException("Invalid compare price");
        }

        if (!ValidationHelper.isValidSKU(sku)) {
            throw new IllegalArgumentException("Invalid SKU format");
        }

        if (!ValidationHelper.isValidWeight(weight)) {
            throw new IllegalArgumentException("Invalid weight");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }

        return Product.builder()
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .comparePrice(comparePrice)
                .sku(sku.toUpperCase())
                .weight(weight)
                .category(category)
                .colours(new ArrayList<>())
                .images(new ArrayList<>())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    /**
     * Creates a new Product with primary image
     */
    public static Product createProductWithImage(String name, String description, double basePrice,
            String sku, Category category, ProductImage primaryImage) {

        Product product = createProduct(name, description, basePrice, sku, category);

        if (primaryImage != null) {
            product.setPrimaryImage(primaryImage);
        }

        return product;
    }

    /**
     * Creates an inactive product (for draft/archived products)
     */
    public static Product createInactiveProduct(String name, String description, double basePrice,
            String sku, Category category) {

        Product product = createProduct(name, description, basePrice, sku, category);
        product.setActive(false);

        return product;
    }
}
