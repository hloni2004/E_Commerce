package za.ac.styling.factory;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.util.ValidationHelper;

import java.util.HashSet;

public class ProductColourFactory {

    public static ProductColour createProductColour(String name, String hexCode, Product product) {

        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Colour name cannot be empty");
        }

        if (!ValidationHelper.isValidHexColor(hexCode)) {
            throw new IllegalArgumentException("Invalid hex code format. Must be in format #RRGGBB");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        return ProductColour.builder()
                .name(name.trim())
                .hexCode(hexCode.toUpperCase())
                .product(product)
                .sizes(new HashSet<>())
                .build();
    }

    public static ProductColour createProductColour(String name, Product product) {
        return createProductColour(name, "#000000", product);
    }

    public static ProductColour createBlackColour(Product product) {
        return createProductColour("Black", "#000000", product);
    }

    public static ProductColour createWhiteColour(Product product) {
        return createProductColour("White", "#FFFFFF", product);
    }

    public static ProductColour createRedColour(Product product) {
        return createProductColour("Red", "#FF0000", product);
    }

    public static ProductColour createBlueColour(Product product) {
        return createProductColour("Blue", "#0000FF", product);
    }

    public static ProductColour createGreenColour(Product product) {
        return createProductColour("Green", "#00FF00", product);
    }

    public static ProductColour createGrayColour(Product product) {
        return createProductColour("Gray", "#808080", product);
    }

    public static ProductColour createNavyColour(Product product) {
        return createProductColour("Navy", "#000080", product);
    }
}
