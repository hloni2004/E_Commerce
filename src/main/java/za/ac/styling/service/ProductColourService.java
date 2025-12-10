package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;

import java.util.List;

/**
 * Service interface for ProductColour entity
 */
public interface ProductColourService extends IService<ProductColour, Integer> {

    /**
     * Find all colours for a product
     */
    List<ProductColour> findByProduct(Product product);

    /**
     * Find all colours for a product by product ID
     */
    List<ProductColour> findByProductId(Integer productId);

    /**
     * Find colour by name and product
     */
    List<ProductColour> findByProductAndName(Product product, String name);

    /**
     * Check if product has specific colour
     */
    boolean hasColour(Product product, String colourName);
}
