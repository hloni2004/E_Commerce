package za.ac.styling.factory;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating ProductImage objects
 */
public class ProductImageFactory {

    /**
     * Creates a new ProductImage with all information
     */
    public static ProductImage createProductImage(Product product, String imageUrl, String supabaseUrl, String bucketPath,
                                                  String altText, int displayOrder,
                                                  boolean isPrimary) {

        // Validate input data
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        if (ValidationHelper.isNullOrEmpty(imageUrl) && ValidationHelper.isNullOrEmpty(supabaseUrl)) {
            throw new IllegalArgumentException("Either Image URL or Supabase URL must be provided");
        }


        if (displayOrder < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        return ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .supabaseUrl(supabaseUrl)
                .bucketPath(bucketPath)
                .altText(altText != null ? altText : product.getName())
                .displayOrder(displayOrder)
                .isPrimary(isPrimary)
                .build();
    }

    /**
     * Creates a new ProductImage with default values
     */
    public static ProductImage createProductImage(Product product, String imageUrl) {
        return createProductImage(product, imageUrl, null, null, null, 0, false);
    }

    /**
     * Creates a primary ProductImage
     */
    public static ProductImage createPrimaryProductImage(Product product, String imageUrl,
                                                        String altText) {
        return createProductImage(product, imageUrl, null, null, altText, 0, true);
    }

    /**
     * Creates a secondary ProductImage with specified display order
     */
    public static ProductImage createSecondaryProductImage(Product product, String imageUrl,
                                                          String altText, int displayOrder) {
        return createProductImage(product, imageUrl, null, null, altText, displayOrder, false);
    }

    /**
     * Updates the display order of an existing ProductImage
     */
    public static ProductImage updateDisplayOrder(ProductImage image, int newDisplayOrder) {
        if (image == null) {
            throw new IllegalArgumentException("ProductImage cannot be null");
        }

        if (newDisplayOrder < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        image.setDisplayOrder(newDisplayOrder);
        return image;
    }

    /**
     * Sets an image as primary
     */
    public static ProductImage setAsPrimary(ProductImage image) {
        if (image == null) {
            throw new IllegalArgumentException("ProductImage cannot be null");
        }

        image.setPrimary(true);
        image.setDisplayOrder(0);
        return image;
    }

    /**
     * Sets an image as non-primary
     */
    public static ProductImage setAsSecondary(ProductImage image, int displayOrder) {
        if (image == null) {
            throw new IllegalArgumentException("ProductImage cannot be null");
        }

        image.setPrimary(false);
        image.setDisplayOrder(displayOrder);
        return image;
    }
}
