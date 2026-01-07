package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;

import java.util.List;

public interface ProductColourService extends IService<ProductColour, Integer> {

    List<ProductColour> findByProduct(Product product);

    List<ProductColour> findByProductId(Integer productId);

    List<ProductColour> findByProductAndName(Product product, String name);

    boolean hasColour(Product product, String colourName);
}
