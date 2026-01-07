package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageService extends IService<ProductImage, Long> {

    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductId(Integer productId);

    List<ProductImage> findByProductOrderByDisplayOrder(Product product);

    Optional<ProductImage> findPrimaryImageByProduct(Product product);

    List<ProductImage> findSecondaryImagesByProduct(Product product);

    ProductImage setAsPrimary(Long imageId);

    ProductImage updateDisplayOrder(Long imageId, int newDisplayOrder);

    int getMaxDisplayOrder(Integer productId);
}