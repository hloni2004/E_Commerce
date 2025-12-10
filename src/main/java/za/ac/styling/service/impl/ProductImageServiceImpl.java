package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.factory.ProductImageFactory;
import za.ac.styling.repository.ProductImageRepository;
import za.ac.styling.service.ProductImageService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for ProductImage entity
 */
@Service
public class ProductImageServiceImpl implements ProductImageService {

    private ProductImageRepository productImageRepository;

    @Autowired
    public ProductImageServiceImpl(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }

    @Override
    public ProductImage create(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }

    @Override
    public ProductImage read(Long id) {
        return productImageRepository.findById(id).orElse(null);
    }

    @Override
    public ProductImage update(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }

    @Override
    public List<ProductImage> getAll() {
        return productImageRepository.findAll();
    }

    @Override
    public List<ProductImage> findByProduct(Product product) {
        return productImageRepository.findByProduct(product);
    }

    @Override
    public List<ProductImage> findByProductId(Integer productId) {
        return productImageRepository.findByProductProductId(productId);
    }

    @Override
    public List<ProductImage> findByProductOrderByDisplayOrder(Product product) {
        return productImageRepository.findByProductOrderByDisplayOrderAsc(product);
    }

    @Override
    public Optional<ProductImage> findPrimaryImageByProduct(Product product) {
        return productImageRepository.findByProductAndIsPrimaryTrue(product);
    }

    @Override
    public List<ProductImage> findSecondaryImagesByProduct(Product product) {
        return productImageRepository.findByProductAndIsPrimaryFalse(product);
    }

    @Override
    public ProductImage setAsPrimary(Long imageId) {
        ProductImage image = read(imageId);
        if (image != null) {
            // First, unset any existing primary images for this product
            List<ProductImage> existingPrimary = productImageRepository.findByProductAndIsPrimaryTrue(image.getProduct()).stream().toList();
            for (ProductImage existing : existingPrimary) {
                existing.setPrimary(false);
                update(existing);
            }

            // Set this image as primary
            ProductImage updated = ProductImageFactory.setAsPrimary(image);
            return update(updated);
        }
        return null;
    }

    @Override
    public ProductImage updateDisplayOrder(Long imageId, int newDisplayOrder) {
        ProductImage image = read(imageId);
        if (image != null) {
            ProductImage updated = ProductImageFactory.updateDisplayOrder(image, newDisplayOrder);
            return update(updated);
        }
        return null;
    }
}