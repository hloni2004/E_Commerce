package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.factory.ProductColourSizeFactory;
import za.ac.styling.repository.ProductColourSizeRepository;
import za.ac.styling.service.ProductColourSizeService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for ProductColourSize entity
 */
@Service
public class ProductColourSizeServiceImpl implements ProductColourSizeService {

    private ProductColourSizeRepository productColourSizeRepository;

    @Autowired
    public ProductColourSizeServiceImpl(ProductColourSizeRepository productColourSizeRepository) {
        this.productColourSizeRepository = productColourSizeRepository;
    }

    @Override
    public ProductColourSize create(ProductColourSize productColourSize) {
        return productColourSizeRepository.save(productColourSize);
    }

    @Override
    public ProductColourSize read(Integer id) {
        return productColourSizeRepository.findById(id).orElse(null);
    }

    @Override
    public ProductColourSize update(ProductColourSize productColourSize) {
        return productColourSizeRepository.save(productColourSize);
    }

    @Override
    public List<ProductColourSize> getAll() {
        return productColourSizeRepository.findAll();
    }

    @Override
    public List<ProductColourSize> findByColour(ProductColour colour) {
        return productColourSizeRepository.findByColour(colour);
    }

    @Override
    public List<ProductColourSize> findByColourId(Integer colourId) {
        return productColourSizeRepository.findByColourColourId(colourId);
    }

    @Override
    public Optional<ProductColourSize> findByColourAndSizeName(ProductColour colour, String sizeName) {
        return productColourSizeRepository.findByColourAndSizeName(colour, sizeName);
    }

    @Override
    public List<ProductColourSize> findLowStockItems() {
        return productColourSizeRepository.findLowStockItems();
    }

    @Override
    public List<ProductColourSize> findOutOfStockItems() {
        return productColourSizeRepository.findOutOfStockItems();
    }

    @Override
    public List<ProductColourSize> findAvailableSizesByColour(ProductColour colour) {
        return productColourSizeRepository.findAvailableSizesByColour(colour);
    }

    @Override
    public ProductColourSize reserveStock(Integer sizeId, int quantity) {
        ProductColourSize size = read(sizeId);
        if (size != null) {
            ProductColourSize updated = ProductColourSizeFactory.reserveStock(size, quantity);
            return update(updated);
        }
        return null;
    }

    @Override
    public ProductColourSize releaseStock(Integer sizeId, int quantity) {
        ProductColourSize size = read(sizeId);
        if (size != null) {
            ProductColourSize updated = ProductColourSizeFactory.releaseStock(size, quantity);
            return update(updated);
        }
        return null;
    }

    @Override
    public ProductColourSize completeSale(Integer sizeId, int quantity) {
        ProductColourSize size = read(sizeId);
        if (size != null) {
            ProductColourSize updated = ProductColourSizeFactory.completeSale(size, quantity);
            return update(updated);
        }
        return null;
    }

    @Override
    public ProductColourSize addStock(Integer sizeId, int quantity) {
        ProductColourSize size = read(sizeId);
        if (size != null) {
            ProductColourSize updated = ProductColourSizeFactory.addStock(size, quantity);
            return update(updated);
        }
        return null;
    }

    @Override
    public boolean needsReordering(Integer sizeId) {
        ProductColourSize size = read(sizeId);
        return ProductColourSizeFactory.needsReordering(size);
    }

    @Override
    public void delete(Integer id) {
        productColourSizeRepository.deleteById(id);
    }
}