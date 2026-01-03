package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.repository.ProductRepository;
import za.ac.styling.repository.ProductImageRepository;
import za.ac.styling.service.ProductService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Product entity
 */
@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product read(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product readWithRelations(Integer id) {
        return productRepository.findByIdWithRelations(id).orElse(null);
    }

    @Override
    public Product update(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAllWithRelations() {
        return productRepository.findAllWithRelations();
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    @Override
    public List<Product> findByCategoryIdWithRelations(Long categoryId) {
        return productRepository.findByCategoryCategoryIdWithRelations(categoryId);
    }

    @Override
    public List<Product> findActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    @Override
    public List<Product> findActiveProductsByCategory(Category category) {
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    @Override
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByBasePriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Product> findActiveProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByBasePriceBetweenAndIsActiveTrue(minPrice, maxPrice);
    }

    @Override
    public List<Product> findLatestProducts() {
        return productRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Product> findProductsSortedByPriceAsc() {
        return productRepository.findByIsActiveTrueOrderByBasePriceAsc();
    }

    @Override
    public List<Product> findProductsSortedByPriceDesc() {
        return productRepository.findByIsActiveTrueOrderByBasePriceDesc();
    }

    @Override
    public Product activateProduct(Integer productId) {
        Product product = read(productId);
        if (product != null) {
            product.setActive(true);
            return update(product);
        }
        return null;
    }

    @Override
    public Product deactivateProduct(Integer productId) {
        Product product = read(productId);
        if (product != null) {
            product.setActive(false);
            return update(product);
        }
        return null;
    }

    @Override
    public ProductImage getImageById(Long imageId) {
        return productImageRepository.findById(imageId).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }
}
