package za.ac.styling.factory;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.util.ValidationHelper;

public class ProductImageFactory {

    public static ProductImage createProductImage(Product product, String imageUrl, String supabaseUrl, String bucketPath,
                                                  String altText, int displayOrder,
                                                  boolean isPrimary) {

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

    public static ProductImage createProductImage(Product product, String imageUrl) {
        return createProductImage(product, imageUrl, null, null, null, 0, false);
    }

    public static ProductImage createPrimaryProductImage(Product product, String imageUrl,
                                                        String altText) {
        return createProductImage(product, imageUrl, null, null, altText, 0, true);
    }

    public static ProductImage createSecondaryProductImage(Product product, String imageUrl,
                                                          String altText, int displayOrder) {
        return createProductImage(product, imageUrl, null, null, altText, displayOrder, false);
    }

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

    public static ProductImage setAsPrimary(ProductImage image) {
        if (image == null) {
            throw new IllegalArgumentException("ProductImage cannot be null");
        }

        image.setPrimary(true);
        image.setDisplayOrder(0);
        return image;
    }

    public static ProductImage setAsSecondary(ProductImage image, int displayOrder) {
        if (image == null) {
            throw new IllegalArgumentException("ProductImage cannot be null");
        }

        image.setPrimary(false);
        image.setDisplayOrder(displayOrder);
        return image;
    }
}
